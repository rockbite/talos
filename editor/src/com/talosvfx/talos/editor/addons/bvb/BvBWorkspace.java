package com.talosvfx.talos.editor.addons.bvb;

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
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.project.FileTracker;
import com.talosvfx.talos.editor.widgets.propertyWidgets.CheckboxWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.FloatPropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.ui.ViewportWidget;
import com.talosvfx.talos.runtime.ParticleEffectDescriptor;
import com.talosvfx.talos.runtime.ParticleEffectInstance;
import com.talosvfx.talos.runtime.render.SpriteBatchParticleRenderer;

import java.io.File;
import java.io.StringWriter;

public class BvBWorkspace extends ViewportWidget implements Json.Serializable, IPropertyProvider {

    public final BvBAddon bvb;
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

    public FloatPropertyWidget spineScaleWidget;

    private int selectIndex = 0;

    BvBWorkspace(BvBAddon bvb) {
        setSkin(TalosMain.Instance().getSkin());
        this.bvb = bvb;
        setModeUI();

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

                Array<AttachmentPoint> possiblePoints = new Array<>();
                ObjectMap<AttachmentPoint, BoundEffect> pointEffectMap = new ObjectMap<>();

                // check for all attachment points
                for(BoundEffect effect: skeletonContainer.getBoundEffects()) {
                    AttachmentPoint position = effect.getPositionAttachment();
                    Array<AttachmentPoint> attachments = effect.getAttachments();

                    tmp = getAttachmentPosition(position);
                    if(tmp.dst(pos) < pixelToWorld(10f)) {
                        possiblePoints.add(position);
                        pointEffectMap.put(position, effect);
                    }

                    for(AttachmentPoint point: attachments) {
                        tmp = getAttachmentPosition(point);
                        if(tmp.dst(pos) < pixelToWorld(10f)) {
                            possiblePoints.add(point);
                            pointEffectMap.put(point, effect);
                        }
                    }
                }

                if(possiblePoints.size > 0) {
                    AttachmentPoint point = possiblePoints.get(selectIndex % possiblePoints.size);
                    BoundEffect effect = pointEffectMap.get(point);
                    movingPoint = point;
                    event.handle();
                    effectSelected(effect);
                    selectIndex++;
                    return true;
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
                        Bone boneByName = skeletonContainer.getBoneByName(movingPoint.getBoneName());
                        float boneWorldScale = skeletonContainer.getSkeleton().getScaleX();
                        pos.sub(boneByName.getWorldX(), boneByName.getWorldY());
                        pos.rotate(-skeletonContainer.getBoneRotation(movingPoint.getBoneName()));
                        movingPoint.setOffset(pos.x / boneWorldScale, pos.y / boneWorldScale);
                        bvb.properties.updateValues();
                    } else {
                        Bone closestBone = skeletonContainer.findClosestBone(pos);
                        float boneWorldScale = skeletonContainer.getSkeleton().getScaleX();
                        pos.sub(closestBone.getWorldX(), closestBone.getWorldY());
                        pos.rotate(-closestBone.getWorldRotationX());
                        movingPoint.setOffset(pos.x / boneWorldScale, pos.y / boneWorldScale);
                        movingPoint.setBone(closestBone.getData().getName());
                        bvb.properties.updateValues();
                    }
                }
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

                if(movingPoint != null) {
                    TalosMain.Instance().ProjectController().setDirty();
                }

                movingPoint = null;
            }

            @Override
            public boolean keyDown(InputEvent event, int keycode) {

                if(keycode == Input.Keys.F5) {
                    // find particle or emitter or then any other module and focus on it
                    camera.position.set(0, 0, 0);
                    setWorldSize(getWorldWidth());
                }
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

                if(keycode == Input.Keys.Z && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && !Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                    TalosMain.Instance().ProjectController().undo();
                }

                if(keycode == Input.Keys.Z && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                    TalosMain.Instance().ProjectController().redo();
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
            float width = pixelToWorld(29f);
            float height = pixelToWorld(29f);
            float rotation = bone.getWorldRotationX() - 90f;
            float originX = pixelToWorld(15);
            float originY = pixelToWorld(15);

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
            tmp.set(point.getWorldOffsetX(), point.getWorldOffsetY());
            tmp.rotate(skeletonContainer.getBoneRotation(point.getBoneName()));
            tmp.add(skeletonContainer.getBonePosX(point.getBoneName()), skeletonContainer.getBonePosY(point.getBoneName()));
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
        if(skeletonContainer.getSkeleton() == null) return null;
        pathMap.put(handle.name(), handle.path());

        registerTalosAssets(handle);

        String name = handle.nameWithoutExtension();
        ParticleEffectDescriptor descriptor = new ParticleEffectDescriptor();
        descriptor.setAssetProvider(TalosMain.Instance().TalosProject().getProjectAssetProvider());
        descriptor.load(handle);
        vfxLibrary.put(name, descriptor);

        BoundEffect effect = skeletonContainer.addEffect(name, descriptor);
        effect.setPositionAttachment(skeletonContainer.getSkeleton().getRootBone().toString());

        TalosMain.Instance().ProjectController().setDirty();

        return effect;
    }

    public void registerTalosAssets(FileHandle handle) {
        JsonReader jsonReader = new JsonReader();
        final JsonValue parse = jsonReader.parse(handle);
        final JsonValue metaData = parse.get("metadata");
        final JsonValue resourcePaths = metaData.get("resources");
        for(JsonValue path: resourcePaths) {
            String name = path.asString();
            String possiblePath = handle.parent() + File.separator + name + ".png"; // this is handling only PNG's which is bad
            FileHandle fileHandle = TalosMain.Instance().ProjectController().findFile(possiblePath);
            TalosMain.Instance().FileTracker().trackFile(fileHandle, new FileTracker.Tracker() {
                @Override
                public void updated(FileHandle handle) {
                    // this is not good either... but whatever
                }
            });
        }
    }

    public void updateParticle(FileHandle handle) {
        String name = handle.nameWithoutExtension();
        if(vfxLibrary.containsKey(name)) {
            ParticleEffectDescriptor descriptor = new ParticleEffectDescriptor();
            descriptor.setAssetProvider(TalosMain.Instance().TalosProject().getProjectAssetProvider());
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
        json.writeValue("spineScale", getSpineScale());
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
        float scl = jsonData.getFloat("spineScale", 1);
        spineScaleWidget.setValue(scl);
        skeletonContainer.setScale(1f/scl, 1f/scl);
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

        spineScaleWidget = new FloatPropertyWidget("spine scale") {
            @Override
            public Float getValue() {
                return getSpineScale();
            }

            @Override
            public void valueChanged(Float value) {
                setSpineScale(value);
            }
        };


        properties.add(preMultipliedAlphaWidget);
        properties.add(speed);
        properties.add(worldWidthWidget);
        properties.add(spineScaleWidget);

        return properties;
    }

    private void setSpineScale(Float scale) {
        if(skeletonContainer == null || skeletonContainer.getSkeleton() == null) return;
        skeletonContainer.getSkeleton().setScale(1f/scale, 1f/scale);
    }

    private Float getSpineScale() {
        if(skeletonContainer == null || skeletonContainer.getSkeleton() == null) return 1f;
        return 1f/skeletonContainer.getSkeleton().getScaleX();
    }

    @Override
    public String getPropertyBoxTitle() {
        return "Workspace";
    }

    @Override
    public int getPriority() {
        return 0;
    }

    public SkeletonContainer getSkeletonContainer() {
        return skeletonContainer;
    }
}
