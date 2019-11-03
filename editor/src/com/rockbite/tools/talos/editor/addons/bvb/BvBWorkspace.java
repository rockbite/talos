package com.rockbite.tools.talos.editor.addons.bvb;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.spine.*;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.rockbite.tools.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.rockbite.tools.talos.editor.widgets.ui.ViewportWidget;
import com.rockbite.tools.talos.runtime.ParticleEffectDescriptor;
import com.rockbite.tools.talos.runtime.ParticleEffectInstance;
import com.rockbite.tools.talos.runtime.render.SpriteBatchParticleRenderer;

public class BvBWorkspace extends ViewportWidget implements Json.Serializable, IPropertyProvider {

    private final BvBAddon bvb;
    private BvBAssetProvider assetProvider;
    private SkeletonContainer skeletonContainer;
    private SpriteBatchParticleRenderer talosRenderer;
    private SkeletonRenderer renderer;

    private AttachmentPoint movingPoint;

    private boolean paused = false;
    private boolean showingTools = false;
    private float speedMultiplier = 1f;

    private ObjectMap<String, ParticleEffectDescriptor> vfxLibrary = new ObjectMap<>();
    private ObjectMap<String, String> pathMap = new ObjectMap<>();

    private BoundEffect selectedEffect = null;

    private Label hintLabel;

    private Vector2 tmp = new Vector2();
    private Vector2 tmp2 = new Vector2();
    private Vector2 tmp3 = new Vector2();

    BvBWorkspace(BvBAddon bvb) {
        this.bvb = bvb;
        setModeUI();

        assetProvider = new BvBAssetProvider();
        skeletonContainer = new SkeletonContainer(this);

        talosRenderer = new SpriteBatchParticleRenderer(null);

        renderer = new SkeletonRenderer();
        renderer.setPremultipliedAlpha(false); // PMA results in correct blending without outlines. (actually should be true, not sure why this ruins scene2d later, probably blend screwup, will check later)

        setCameraPos(0, 0);
        bgColor.set(0.1f, 0.1f, 0.1f, 1f);

        hintLabel = new Label("", TalosMain.Instance().getSkin());
        add(hintLabel).left().expandX().pad(5f);
        row();
        add().expand();
        row();

        clearListeners();
        addListeners();
        addPanListener();

        bvb.properties.showPanel(this);
        bvb.properties.showPanel(skeletonContainer);
    }

    private void addListeners() {
        addListener(new ClickListener() {

            private Vector3 tmp3 = new Vector3();
            private Vector2 pos = new Vector2();
            private Vector2 tmp = new Vector2();

            boolean stageClick;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                getWorldFromLocal(tmp3.set(x, y, 0));
                pos.set(tmp3.x, tmp3.y);

                stageClick = false;

                getStage().setKeyboardFocus(BvBWorkspace.this);

                if(skeletonContainer.getSkeleton() == null) return false;

                // check for all attachment points
                for(BoundEffect effect: skeletonContainer.getBoundEffects()) {
                    AttachmentPoint position = effect.getPositionAttachment();
                    Array<AttachmentPoint> attachments = effect.getAttachments();

                    tmp = getAttachmentPosition(position);
                    if(tmp.dst(pos) < pixelToWorld(10f)) {
                        movingPoint = position;
                        event.handle();
                        effectSelected(effect);
                        return true;
                    }

                    for(AttachmentPoint point: attachments) {
                        tmp = getAttachmentPosition(point);
                        if(tmp.dst(pos) < pixelToWorld(10f)) {
                            movingPoint = point;
                            event.handle();
                            effectSelected(effect);
                            return true;
                        }
                    }
                }

                stageClick = true;

                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                super.touchDragged(event, x, y, pointer);

                getWorldFromLocal(tmp3.set(x, y, 0));
                pos.set(tmp3.x, tmp3.y);

                if(movingPoint != null && !movingPoint.isStatic()) {

                    if(Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT)) {
                        pos.sub(skeletonContainer.getBonePosX(movingPoint.getBoneName()), skeletonContainer.getBonePosY(movingPoint.getBoneName()));
                        movingPoint.setOffset(pos.x, pos.y);
                    } else {
                        Bone closestBone = skeletonContainer.findClosestBone(pos);
                        pos.sub(closestBone.getWorldX(), closestBone.getWorldY());
                        movingPoint.setOffset(pos.x, pos.y);
                        movingPoint.setBone(closestBone.getData().getName());
                    }
                }

                TalosMain.Instance().ProjectController().setDirty();
            }

            @Override
            public boolean mouseMoved(InputEvent event, float x, float y) {
                getWorldFromLocal(tmp3.set(x, y, 0));
                pos.set(tmp3.x, tmp3.y);

                if(skeletonContainer.getSkeleton() != null) {
                    Bone closestBone = skeletonContainer.findClosestBone(pos);
                    float dist = skeletonContainer.getBoneDistance(closestBone, pos);

                    if (dist < pixelToWorld(10f)) {
                        hintLabel.setText(closestBone.getData().getName());
                    } else {
                        hintLabel.setText("");
                    }
                }

                return super.mouseMoved(event, x, y);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);

                if(stageClick) {
                    if(selectedEffect != null) {
                        effectUnselected(selectedEffect);
                    }
                }

                movingPoint = null;
            }

            @Override
            public boolean keyDown(InputEvent event, int keycode) {

                if(keycode == Input.Keys.SPACE) {
                    paused = !paused;
                }
                if(keycode == Input.Keys.SHIFT_LEFT) {
                    showingTools = !showingTools;
                }

                return super.keyDown(event, keycode);
            }
        });
    }

    private void effectSelected(BoundEffect effect) {
        selectedEffect = effect;
        bvb.properties.showPanel(effect);
    }

    private void effectUnselected(BoundEffect effect) {
        selectedEffect = null;
        bvb.properties.hidePanel(effect);
    }

    public void setModeUI() {
        setWorldSize(1280f);
    }

    public void setModeGame() {
        setWorldSize(10f);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if(skeletonContainer != null) {
            skeletonContainer.update(delta, paused);
        }
    }

    @Override
    public void drawContent(Batch batch, float parentAlpha) {
        batch.end();
        drawGrid(batch, parentAlpha);
        batch.begin();

        drawSpine(batch, parentAlpha);
        drawVFX(batch, parentAlpha);


        drawTools(batch, parentAlpha);

    }

    private void drawTools(Batch batch, float parentAlpha) {
        Skeleton skeleton = skeletonContainer.getSkeleton();
        if(skeleton == null) return;

        if(showingTools) {
            batch.end();
            Gdx.gl.glLineWidth(1f);
            Gdx.gl.glEnable(GL20.GL_BLEND);
            shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

            drawShapeRendererTools();

            shapeRenderer.end();
            batch.begin();

            if(showingTools) {
                drawSpriteTools(batch, parentAlpha);
            }
        }
    }

    private void drawShapeRendererTools() {
        /**
         * Drawing bones
         */
        Skeleton skeleton = skeletonContainer.getSkeleton();
        shapeRenderer.setColor(Color.RED);
        for (Bone bone : skeleton.getBones()) {
            shapeRenderer.circle(bone.getWorldX(), bone.getWorldY(), pixelToWorld(3f));
        }


        /**
         * Draw bound effects and their attachment points
         */
        for(BoundEffect effect: skeletonContainer.getBoundEffects()) {
            // position attachment first
            AttachmentPoint positionAttachment = effect.getPositionAttachment();
            if(positionAttachment != null && !positionAttachment.isStatic()) {
                Vector2 pos = getAttachmentPosition(positionAttachment);
                shapeRenderer.setColor(Color.BLUE);
                shapeRenderer.circle(pos.x, pos.y, pixelToWorld(5f));
            }

            // now iterate through other non static attachments
            shapeRenderer.setColor(Color.GREEN);
            for(AttachmentPoint point: effect.getAttachments()) {
                if(!point.isStatic()) {
                    Vector2 pos = getAttachmentPosition(point);
                    shapeRenderer.circle(pos.x, pos.y, pixelToWorld(5f));
                }
            }
        }

        /**
         * If attachment point of an effect is currently being moved, then draw line to it's origin or nearest bone
         */
        if(movingPoint != null && !movingPoint.isStatic()) {
            tmp2.set(getAttachmentPosition(movingPoint));

            if(Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT)) {
                Bone bone = skeletonContainer.getBoneByName(movingPoint.getBoneName());
                tmp3.set(bone.getWorldX(), bone.getWorldY());

                shapeRenderer.setColor(Color.PURPLE);
                shapeRenderer.rectLine(tmp2.x, tmp2.y, tmp3.x, tmp3.y, pixelToWorld(2f));
            } else {
                Bone bone = skeletonContainer.findClosestBone(tmp2);
                tmp3.set(bone.getWorldX(), bone.getWorldY());

                shapeRenderer.setColor(Color.WHITE);
                shapeRenderer.rectLine(tmp2.x, tmp2.y, tmp3.x, tmp3.y, pixelToWorld(2f));
            }

        }
    }

    private Vector2 getAttachmentPosition(AttachmentPoint point) {
        if(!point.isStatic()) {
            tmp.set(skeletonContainer.getBonePosX(point.getBoneName()), skeletonContainer.getBonePosY(point.getBoneName()));
            tmp.add(point.getOffsetX(), point.getOffsetY());
        } else{
            tmp.set(point.getStaticValue().get(0), point.getStaticValue().get(1));
        }
        return tmp;
    }

    private void drawSpriteTools(Batch batch, float parentAlpha) {

    }

    private void drawSpine(Batch batch, float parentAlpha) {
        Skeleton skeleton = skeletonContainer.getSkeleton();
        AnimationState animationState = skeletonContainer.getAnimationState();
        if(skeleton == null) return;

        skeleton.setPosition(0, 0);
        skeleton.updateWorldTransform(); // Uses the bones' local SRT to compute their world SRT.


        int a1 = batch.getBlendSrcFunc();
        int a2 = batch.getBlendDstFunc();
        int a3 = batch.getBlendSrcFuncAlpha();
        int a4 = batch.getBlendDstFuncAlpha();
        renderer.draw(batch, skeleton); // Draw the skeleton images.

        // fixing back the blending because PMA is shit
        batch.setBlendFunctionSeparate(a1, a2, a3, a4);
    }

    private void drawVFX(Batch batch, float parentAlpha) {
        Skeleton skeleton = skeletonContainer.getSkeleton();
        if(skeleton == null) return;

        talosRenderer.setBatch(batch);
        for(BoundEffect effect: skeletonContainer.getBoundEffects()) {
            for(ParticleEffectInstance particleEffectInstance: effect.getParticleEffects()) {
                talosRenderer.render(particleEffectInstance);
            }
        }
    }

    public void setSkeleton(FileHandle jsonFileHandle) {
        pathMap.put(jsonFileHandle.name(), jsonFileHandle.path());

        FileHandle atlasFileHandle = Gdx.files.absolute(jsonFileHandle.pathWithoutExtension() + ".atlas");
        jsonFileHandle = TalosMain.Instance().ProjectController().findFile(jsonFileHandle);
        atlasFileHandle = TalosMain.Instance().ProjectController().findFile(atlasFileHandle);

        skeletonContainer.setSkeleton(jsonFileHandle, atlasFileHandle);

        TalosMain.Instance().ProjectController().setDirty();
    }

    public BoundEffect addParticle(FileHandle handle) {
        pathMap.put(handle.name(), handle.path());

        String name = handle.nameWithoutExtension();
        ParticleEffectDescriptor descriptor = new ParticleEffectDescriptor();
        assetProvider.setParticleFolder(handle.parent().path());
        descriptor.setAssetProvider(assetProvider);
        descriptor.load(handle);
        vfxLibrary.put(name, descriptor);

        BoundEffect effect = skeletonContainer.addEffect(name, descriptor);
        effect.setPositionAttachment(skeletonContainer.getSkeleton().getRootBone().toString());

        TalosMain.Instance().ProjectController().setDirty();

        return effect;
    }

    public void updateParticle(FileHandle handle) {
        String name = handle.nameWithoutExtension();
        if(vfxLibrary.containsKey(name)) {
            ParticleEffectDescriptor descriptor = new ParticleEffectDescriptor();
            assetProvider.setParticleFolder(handle.parent().path());
            descriptor.setAssetProvider(assetProvider);
            descriptor.load(handle);
            vfxLibrary.put(name, descriptor);

            skeletonContainer.updateEffect(name, descriptor);
        }
    }

    @Override
    public void write(Json json) {
        json.writeValue("skeleton", skeletonContainer);
        json.writeObjectStart("paths");
        for(String fileName: pathMap.keys()) {
            json.writeValue(fileName, pathMap.get(fileName));
        }
        json.writeObjectEnd();
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        cleanWorkspace();
        JsonValue paths = jsonData.get("paths");
        pathMap.clear();
        for(JsonValue path: paths) {
            pathMap.put(path.name(), path.asString());
        }

        skeletonContainer = new SkeletonContainer(this);
        skeletonContainer.read(json, jsonData.get("skeleton"));

        bvb.properties.showPanel(skeletonContainer);
    }

    public void cleanWorkspace() {
        pathMap.clear();
        skeletonContainer.clear();
    }

    public String getPath(String fileName) {
        return pathMap.get(fileName);
    }

    public BvBAssetProvider getAssetProvider() {
        return assetProvider;
    }

    public ObjectMap<String, ParticleEffectDescriptor> getVfxLibrary() {
        return vfxLibrary;
    }

    @Override
    public Array<PropertyWidget> getListOfProperties() {
        return null;
    }

    @Override
    public String getPropertyBoxTitle() {
        return "Workspace";
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
