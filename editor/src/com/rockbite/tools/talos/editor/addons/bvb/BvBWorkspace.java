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
import com.badlogic.gdx.utils.*;
import com.esotericsoftware.spine.*;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.widgets.propertyWidgets.CheckboxWidget;
import com.rockbite.tools.talos.editor.widgets.propertyWidgets.FloatPropertyWidget;
import com.rockbite.tools.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.rockbite.tools.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.rockbite.tools.talos.editor.widgets.ui.ViewportWidget;
import com.rockbite.tools.talos.runtime.ParticleEffectDescriptor;
import com.rockbite.tools.talos.runtime.ParticleEffectInstance;
import com.rockbite.tools.talos.runtime.render.SpriteBatchParticleRenderer;

import java.io.StringWriter;

public class BvBWorkspace extends ViewportWidget implements Json.Serializable, IPropertyProvider {

    public final BvBAddon bvb;
    private BvBAssetProvider assetProvider;
    private SkeletonContainer skeletonContainer;
    private SpriteBatchParticleRenderer talosRenderer;
    private SkeletonRenderer renderer;

    private AttachmentPoint movingPoint;

    private boolean paused = false;
    private boolean showingTools = false;

    private float speedMultiplier = 1f;
    private boolean preMultipliedAlpha = false;

    private ObjectMap<String, ParticleEffectDescriptor> vfxLibrary = new ObjectMap<>();
    private ObjectMap<String, String> pathMap = new ObjectMap<>();

    public BoundEffect selectedEffect = null;

    private Label hintLabel;

    private Vector2 tmp = new Vector2();
    private Vector2 tmp2 = new Vector2();
    private Vector2 tmp3 = new Vector2();

    BvBWorkspace(BvBAddon bvb) {
        setSkin(TalosMain.Instance().getSkin());
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

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                getWorldFromLocal(tmp3.set(x, y, 0));
                pos.set(tmp3.x, tmp3.y);

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

                if(selectedEffect != null) {
                    effectUnselected(selectedEffect);
                }

                return false;
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
                        bvb.properties.updateValues();
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

                movingPoint = null;
            }

            @Override
            public boolean keyDown(InputEvent event, int keycode) {

                if(keycode == Input.Keys.SPACE) {
                    paused = !paused;
                }
                if(keycode == Input.Keys.DEL || keycode == Input.Keys.FORWARD_DEL) {
                    if(selectedEffect != null) {
                        skeletonContainer.removeEffect(selectedEffect);
                        effectUnselected(selectedEffect);
                    }
                }
                if(keycode == Input.Keys.ENTER) {
                    camera.position.set(0, 0, 0);
                    setWorldSize(getWorldWidth());
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

    public void effectUnselected(BoundEffect effect) {
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
            skeletonContainer.update(delta * speedMultiplier, paused);
        }
    }

    @Override
    public void drawContent(Batch batch, float parentAlpha) {
        batch.end();
        drawGrid(batch, parentAlpha);
        batch.begin();

        drawVFXBefore(batch, parentAlpha);
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

    private void drawSpriteTools(Batch batch, float parentAlpha) {
        /**
         * Drawing bones
         */
        Skeleton skeleton = skeletonContainer.getSkeleton();
        for (Bone bone : skeleton.getBones()) {
            //shapeRenderer.circle(bone.getWorldX(), bone.getWorldY(), pixelToWorld(3f));

            batch.setColor(1f, 1f, 1f, 1f);
            float width = pixelToWorld(10f);
            float height = pixelToWorld(30f);
            float rotation = bone.getWorldRotationX() - 90f;
            float originX = pixelToWorld(5);
            float originY = pixelToWorld(8);

            batch.draw(getSkin().getRegion("bone"), bone.getWorldX() - originX, bone.getWorldY() - originY, originX, originY, width, height, 1f, 1f, rotation);
        }

        /**
         * Draw bound effects and their attachment points
         */
        for(BoundEffect effect: skeletonContainer.getBoundEffects()) {
            // position attachment first
            AttachmentPoint positionAttachment = effect.getPositionAttachment();
            if(positionAttachment != null && !positionAttachment.isStatic()) {
                Vector2 pos = getAttachmentPosition(positionAttachment);

                batch.setColor(1f, 1f, 1f, 1f);
                float size = pixelToWorld(12f);
                batch.draw(getSkin().getRegion("vfx-red"), pos.x-size/2f, pos.y-size/2f, size, size);
            }

            // now iterate through other non static attachments
            shapeRenderer.setColor(Color.GREEN);
            for(AttachmentPoint point: effect.getAttachments()) {
                if(!point.isStatic()) {
                    Vector2 pos = getAttachmentPosition(point);

                    batch.setColor(1f, 1f, 1f, 1f);
                    float size = pixelToWorld(12f);
                    batch.draw(getSkin().getRegion("vfx-green"), pos.x-size/2f, pos.y-size/2f, size, size);
                }
            }
        }
    }


    private void drawShapeRendererTools() {
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
        renderer.setPremultipliedAlpha(preMultipliedAlpha);
        renderer.draw(batch, skeleton); // Draw the skeleton images.

        // fixing back the blending because PMA is shit
        batch.setBlendFunctionSeparate(a1, a2, a3, a4);
    }

    private void drawVFXBefore(Batch batch, float parentAlpha) {
        Skeleton skeleton = skeletonContainer.getSkeleton();
        if(skeleton == null) return;

        talosRenderer.setBatch(batch);
        for(BoundEffect effect: skeletonContainer.getBoundEffects()) {
            if(!effect.isBehind()) continue;
            for(ParticleEffectInstance particleEffectInstance: effect.getParticleEffects()) {
                talosRenderer.render(particleEffectInstance);
            }
        }
    }

    private void drawVFX(Batch batch, float parentAlpha) {
        Skeleton skeleton = skeletonContainer.getSkeleton();
        if(skeleton == null) return;

        talosRenderer.setBatch(batch);
        for(BoundEffect effect: skeletonContainer.getBoundEffects()) {
            if(effect.isBehind()) continue;
            for(ParticleEffectInstance particleEffectInstance: effect.getParticleEffects()) {
                talosRenderer.render(particleEffectInstance);
            }
        }
    }

    public void setSkeleton(FileHandle jsonFileHandle) {
        pathMap.put(jsonFileHandle.name(), jsonFileHandle.path());

        skeletonContainer.setSkeleton(jsonFileHandle);

        bvb.properties.updateValues();
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

    /**
     * jesus it took a while to figure this out... like 8 minutes
     *
     * @return final json string
     */
    public String writeExport() {
        try {
            StringWriter stringWriter = new StringWriter();
            Json json = new Json();
            json .setOutputType(JsonWriter.OutputType.json);
            json.setWriter(stringWriter);
            json.getWriter().object();

            writeExport(json);

            return stringWriter.toString() + "}";
        } catch (Exception e) {
            System.out.println(e);
        }

        return "";
    }

    private void writeExport(Json json) {
        json.writeObjectStart("skeleton");
        skeletonContainer.writeExport(json);
        json.writeObjectEnd();

        json.writeValue("pma", preMultipliedAlpha);

        json.writeObjectStart("metadata");
        json.writeArrayStart("assets");
        Array<String> result = skeletonContainer.getUsedParticleEffectNames();
        for(String name: result) {
            json.writeObjectStart();
            json.writeValue("name", name);
            json.writeValue("type", "vfx");
            json.writeObjectEnd();
        }
        json.writeArrayEnd();
        json.writeObjectEnd();
    }

    @Override
    public void write(Json json) {
        json.writeValue("skeleton", skeletonContainer);
        json.writeObjectStart("paths");
        for(String fileName: pathMap.keys()) {
            json.writeValue(fileName, pathMap.get(fileName));
        }
        json.writeObjectEnd();
        json.writeValue("pma", preMultipliedAlpha);
        json.writeValue("speed", speedMultiplier);
        json.writeValue("worldSize", getWorldWidth());
        json.writeValue("zoom", camera.zoom);
        json.writeValue("cameraPosX", camera.position.x);
        json.writeValue("cameraPosY", camera.position.y);
        if(selectedEffect != null) {
            json.writeValue("selectedEffect", selectedEffect.name);
        }
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

        preMultipliedAlpha = jsonData.getBoolean("pma", false);
        speedMultiplier = jsonData.getFloat("speed", 1f);
        setWorldSize(jsonData.getFloat("worldSize", 1280));
        camera.zoom = jsonData.getFloat("zoom", camera.zoom);
        camera.position.x = jsonData.getFloat("cameraPosX", 0);
        camera.position.y = jsonData.getFloat("cameraPosY", 0);

        bvb.properties.cleanPanels();
        bvb.properties.showPanel(this);
        bvb.properties.showPanel(skeletonContainer);

        String selectedEffect = jsonData.getString("selectedEffect", null);
        BoundEffect effect = skeletonContainer.getEffectByName(selectedEffect);
        if(effect != null) effectSelected(effect);
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
        Array<PropertyWidget> properties = new Array<>();


        CheckboxWidget preMultipliedAlphaWidget = new CheckboxWidget("premultiplied alpha") {
            @Override
            public Boolean getValue() {
                return preMultipliedAlpha;
            }

            @Override
            public void valueChanged(Boolean value) {
                preMultipliedAlpha = value;
            }
        };

        FloatPropertyWidget speed = new FloatPropertyWidget("speed multiplier") {
            @Override
            public Float getValue() {
                return speedMultiplier;
            }

            @Override
            public void valueChanged(Float value) {
                speedMultiplier = value;
            }
        };

        FloatPropertyWidget worldWidthWidget = new FloatPropertyWidget("world width") {
            @Override
            public Float getValue() {
                return getWorldWidth();
            }

            @Override
            public void valueChanged(Float value) {
                setWorldSize(value);
            }
        };

        properties.add(preMultipliedAlphaWidget);
        properties.add(speed);
        properties.add(worldWidthWidget);

        return properties;
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
