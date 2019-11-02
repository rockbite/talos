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
import com.esotericsoftware.spine.*;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.widgets.ui.ViewportWidget;
import com.rockbite.tools.talos.runtime.ParticleEffectDescriptor;
import com.rockbite.tools.talos.runtime.ParticleEffectInstance;
import com.rockbite.tools.talos.runtime.render.SpriteBatchParticleRenderer;

public class BvBWorkspace extends ViewportWidget {

    private BvBAssetProvider assetProvider;
    private SkeletonContainer skeletonContainer = new SkeletonContainer();
    private SpriteBatchParticleRenderer talosRenderer;
    private SkeletonRenderer renderer;

    private AttachmentPoint movingPoint;

    private boolean paused = false;
    private boolean showingTools = false;
    private float speedMultiplier = 1f;

    private Array<ParticleEffectDescriptor> vfxLibrary = new Array<>();

    private Label hintLabel;

    private Vector2 tmp = new Vector2();
    private Vector2 tmp2 = new Vector2();
    private Vector2 tmp3 = new Vector2();

    BvBWorkspace() {
        setModeUI();

        assetProvider = new BvBAssetProvider();

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

                // check for all attachment points
                for(BoundEffect effect: skeletonContainer.getBoundEffects()) {
                    AttachmentPoint position = effect.getPositionAttachment();
                    Array<AttachmentPoint> attachments = effect.getAttachments();

                    tmp = getAttachmentPosition(position);
                    if(tmp.dst(pos) < pixelToWorld(10f)) {
                        movingPoint = position;
                        event.handle();
                        return true;
                    }

                    for(AttachmentPoint point: attachments) {
                        tmp = getAttachmentPosition(point);
                        if(tmp.dst(pos) < pixelToWorld(10f)) {
                            movingPoint = point;
                            event.handle();
                            return true;
                        }
                    }
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
        talosRenderer.setBatch(batch);
        for(BoundEffect effect: skeletonContainer.getBoundEffects()) {
            for(ParticleEffectInstance particleEffectInstance: effect.getParticleEffects()) {
                talosRenderer.render(particleEffectInstance);
            }
        }
    }

    public void setAnimation(FileHandle jsonFileHandle) {
        FileHandle atlasFileHandle = Gdx.files.absolute(jsonFileHandle.pathWithoutExtension() + ".atlas");
        jsonFileHandle = TalosMain.Instance().ProjectController().findFile(jsonFileHandle);
        atlasFileHandle = TalosMain.Instance().ProjectController().findFile(atlasFileHandle);

        skeletonContainer.setAnimation(jsonFileHandle, atlasFileHandle);
    }

    public BoundEffect addParticleToLibrary(FileHandle handle) {
        ParticleEffectDescriptor descriptor = new ParticleEffectDescriptor();
        assetProvider.setParticleFolder(handle.parent().path());
        descriptor.setAssetProvider(assetProvider);
        descriptor.load(handle);

        vfxLibrary.add(descriptor);

        //remove this
        BoundEffect effect = skeletonContainer.addEffect(descriptor);
        effect.setPositionAttachment(skeletonContainer.getSkeleton().getRootBone().toString());
        return effect;
    }
}
