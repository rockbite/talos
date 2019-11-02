/*******************************************************************************
 * Copyright 2019 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.rockbite.tools.talos.editor.widgets.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.FloatCounter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.PerformanceCounter;
import com.badlogic.gdx.utils.StringBuilder;
import com.badlogic.gdx.utils.TimeUtils;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.wrappers.IDragPointProvider;
import com.rockbite.tools.talos.runtime.ParticleEffectInstance;
import com.rockbite.tools.talos.runtime.render.ParticleRenderer;
import com.rockbite.tools.talos.runtime.render.SpriteBatchParticleRenderer;

public class PreviewWidget extends ViewportWidget {

    Vector2 mid = new Vector2();

    Vector2 temp = new Vector2();

    private ParticleRenderer particleRenderer;

    private SpriteBatchParticleRenderer spriteBatchParticleRenderer;

    private ShapeRenderer shapeRenderer;

    private Color tmpColor = new Color();

    private PreviewImageControllerWidget previewController;

    private Image previewImage = new Image();

    private String countStr = "Particles: ";
    private String trisCountStr = "Triangles: ";
    private String nodeCallsStr = "Node Calls: ";
    private String gpuTimeStr = "GPU: ";
    private String cpuTimeStr = "CPU: ";
    private String msStr = "ms";

    private Label countLbl;
    private Label trisCountLbl;
    private Label nodeCallsLbl;
    private Label gpuTimeLbl;
    private Label cpuTimeLbl;

    private GLProfiler glProfiler = new GLProfiler(Gdx.graphics);
    private FPSLogger fpsLogger = new FPSLogger();
    private PerformanceCounter performanceCounter = new PerformanceCounter("talos");

    private StringBuilder stringBuilder = new StringBuilder();
    private int trisCount = 0;
    private FloatCounter renderTime = new FloatCounter(100);
    private FloatCounter cpuTime = new FloatCounter(100);
    private float fps = 0;

    private Array<DragPoint> dragPoints = new Array<>();
    private IDragPointProvider dragPointProvider = null;

    private String backgroundImagePath = "";

    public PreviewWidget() {
        super();

        setWorldSize(10f);

        spriteBatchParticleRenderer = new SpriteBatchParticleRenderer(null);
        particleRenderer = spriteBatchParticleRenderer;
        shapeRenderer = new ShapeRenderer();
        previewController = new PreviewImageControllerWidget(TalosMain.Instance().getSkin()) {
            @Override
            public void removeImage () {
                super.removeImage();
                previewImage.setDrawable(null);
                backgroundImagePath = "";
            }

            @Override
            public void gridSizeChanged(float size) {
                super.gridSizeChanged(size);
                setWorldSize(10f * size);
                resetCamera();
            }
        };

        countLbl = new Label(countStr, TalosMain.Instance().getSkin());
        trisCountLbl = new Label(trisCountStr, TalosMain.Instance().getSkin());
        nodeCallsLbl = new Label(nodeCallsStr, TalosMain.Instance().getSkin());
        gpuTimeLbl = new Label(gpuTimeStr, TalosMain.Instance().getSkin());
        cpuTimeLbl = new Label(cpuTimeStr, TalosMain.Instance().getSkin());

        countLbl.setColor(Color.GRAY);
        trisCountLbl.setColor(Color.GRAY);
        nodeCallsLbl.setColor(Color.GRAY);
        gpuTimeLbl.setColor(Color.GRAY);
        cpuTimeLbl.setColor(Color.GRAY);

        add(countLbl).left().top().padLeft(5).row();
        add(trisCountLbl).left().top().padLeft(5).row();
        add(nodeCallsLbl).left().top().padLeft(5).row();
        add(cpuTimeLbl).left().top().padLeft(5).row();
        add(gpuTimeLbl).left().top().padLeft(5).row();
        add().expand();
        row();
        add(previewController).bottom().left().growX();

        cameraController.scrollOnly = true; // camera controller can't operate in this shitty custom conditions
        addListener(new InputListener() {

            boolean moving = false;
            private Vector3 tmp = new Vector3();
            private Vector3 tmp2 = new Vector3();
            private Vector2 prevPos = new Vector2();
            private Vector2 pos = new Vector2();

            private DragPoint currentlyDragging = null;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                moving = false;
                getWorldFromLocal(tmp.set(x, y, 0));
                pos.set(tmp.x, tmp.y);
                prevPos.set(x, y);

                //detect drag points
                for(DragPoint point: dragPoints) {
                    if(pos.dst(point.position) < 0.2f * camera.zoom) {
                        // dragging a point
                        currentlyDragging = point;
                        return true;
                    }
                }

                if(button == 1) {
                    moving = true;
                    return true;
                }
                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                super.touchDragged(event, x, y, pointer);

                getWorldFromLocal(tmp.set(x, y, 0)); // I can't really explain
                pos.set(tmp.x, tmp.y);

                if(moving) {
                    final ParticleEffectInstance particleEffect = TalosMain.Instance().TalosProject().getParticleEffect();
                    particleEffect.setPosition(tmp.x, tmp.y);
                } else {
                    getWorldFromLocal(tmp.set(prevPos.x, prevPos.y, 0));
                    getWorldFromLocal(tmp2.set(x, y, 0));

                    if(currentlyDragging == null) {
                        // panning

                        camera.position.sub(tmp2.x-tmp.x, tmp2.y-tmp.y, 0);
                    } else {
                        // dragging a point
                        currentlyDragging.position.add(tmp2.x-tmp.x, tmp2.y-tmp.y);
                        dragPointProvider.dragPointChanged(currentlyDragging);
                    }
                }

                prevPos.set(x, y);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                currentlyDragging = null;
            }

            @Override
            public boolean keyUp(InputEvent event, int keycode) {
                return super.keyUp(event, keycode);
            }
        });
    }


    public void fileDrop (float x, float y, String[] paths) {
        temp.set(x, y);
        (getStage().getViewport()).unproject(temp);
        stageToLocalCoordinates(temp);
        if(this.hit(temp.x, temp.y, false) != null) {
           addPreviewImage(paths);
        }
    }

    public void addPreviewImage (String[] paths) {
        if(paths.length == 1) {

            String resourcePath = paths[0];
            FileHandle fileHandle = Gdx.files.absolute(resourcePath);

            final String extension = fileHandle.extension();

            if (extension.endsWith("png") || extension.endsWith("jpg")) {
                final Texture texture = new Texture(fileHandle);
                TalosMain.Instance().TalosProject().getProjectAssetProvider().addTextureAsTextureRegion(fileHandle.nameWithoutExtension(), texture);
                final TextureRegion textureRegion = new TextureRegion(texture);
                previewImage.setDrawable(new TextureRegionDrawable(textureRegion));
                previewController.setImageWidth(10);

                backgroundImagePath = fileHandle.path();
            }
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        //stupid hack, plz do it normal way
        if(PreviewWidget.this.hasScrollFocus() && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            resetCamera();
        }

        long timeBefore = TimeUtils.nanoTime();
        final ParticleEffectInstance particleEffect = TalosMain.Instance().TalosProject().getParticleEffect();
        particleEffect.update(Gdx.graphics.getDeltaTime());
        cpuTime.put( TimeUtils.timeSinceNanos(timeBefore));

        stringBuilder.clear();
        stringBuilder.append(countStr).append(particleEffect.getParticleCount());
        countLbl.setText(stringBuilder.toString());

        stringBuilder.clear();
        stringBuilder.append(trisCountStr).append(trisCount);
        trisCountLbl.setText(stringBuilder.toString());

        stringBuilder.clear();
        stringBuilder.append(nodeCallsStr).append(particleEffect.getNodeCalls());
        nodeCallsLbl.setText(stringBuilder.toString());

        float rt = renderTime.value/1000000f;
        float cp = cpuTime.value/1000000f;

        rt = (float)Math.round(rt * 10000f) / 10000f;
        cp = (float)Math.round(cp * 10000f) / 10000f;

        stringBuilder.clear();
        stringBuilder.append(gpuTimeStr).append(rt).append(msStr);
        gpuTimeLbl.setText(stringBuilder.toString());

        stringBuilder.clear();
        stringBuilder.append(cpuTimeStr).append(cp).append(msStr);
        cpuTimeLbl.setText(stringBuilder.toString());
    }

    @Override
    public void drawContent(Batch batch, float parentAlpha) {
        batch.end();
        drawGrid(batch, parentAlpha * 0.5f);
        batch.begin();

        mid.set(0, 0);

        float imagePrefWidth = previewImage.getPrefWidth();
        float imagePrefHeight = previewImage.getPrefHeight();
        float scale = imagePrefHeight / imagePrefWidth;

        float imageWidth = previewController.getImageWidth();
        float imageHeight = imageWidth * scale;
        previewController.getPreviewBoxWidth();

        previewImage.setPosition(mid.x - imageWidth / 2, mid.y - imageHeight / 2);
        previewImage.setSize(imageWidth, imageHeight);
        if (previewController.isBackground()) {
            previewImage.draw(batch, parentAlpha);
        }

        spriteBatchParticleRenderer.setBatch(batch);

        batch.flush();
        glProfiler.enable();

        long timeBefore = TimeUtils.nanoTime();

        final ParticleEffectInstance particleEffect = TalosMain.Instance().TalosProject().getParticleEffect();
        particleEffect.render(particleRenderer);

        batch.flush();
        renderTime.put(TimeUtils.timeSinceNanos(timeBefore));
        trisCount = (int) (glProfiler.getVertexCount().value / 3f);
        glProfiler.disable();


        if (!previewController.isBackground()) {
            previewImage.draw(batch, parentAlpha);
        }

        // now for the drag points
        if(dragPoints.size > 0) {
            batch.end();
            tmpColor.set(Color.ORANGE);
            tmpColor.a = 0.8f;
            Gdx.gl.glLineWidth(1f);
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(tmpColor);

            for (DragPoint point : dragPoints) {
                shapeRenderer.circle(point.position.x, point.position.y, 0.1f * camera.zoom, 15);
            }

            shapeRenderer.end();
            batch.begin();
        }
    }

    public GLProfiler getGLProfiler() {
        return glProfiler;
    }

    public void registerForDragPoints(IDragPointProvider dragPointProvider) {
        this.dragPointProvider = dragPointProvider;
        DragPoint[] arr = dragPointProvider.fetchDragPoints();
        dragPoints.clear();
        for(int i = 0; i < arr.length; i++) {
            dragPoints.add(arr[i]);
        }
    }

    public void unregisterDragPoints() {
        this.dragPointProvider = null;
        dragPoints.clear();
    }

    public void unregisterDragPoints(IDragPointProvider dragPointProvider) {
        if(this.dragPointProvider == dragPointProvider) {
            this.dragPointProvider = null;
            dragPoints.clear();
        }
    }

    public String getBackgroundImagePath() {
        return backgroundImagePath;
    }

    public boolean isBackgroundImageInBack() {
        return previewController.isBackground();
    }

    public void setImagebIsBackground(boolean isBackground) {
        previewController.setIsBackground(isBackground);
    }

    public float getBgImageSize() {
        return previewController.getImageWidth();
    }

    public void setBgImageSize(float size) {
        previewController.setImageWidth(size);
    }

    public void setBackgroundImage(String bgImagePath) {
        if(bgImagePath != null) {
            addPreviewImage(new String[] {bgImagePath});
        } else {
            previewImage.setDrawable(null);
            backgroundImagePath = "";
        }
    }

    public void resetToDefaults() {
        previewImage.setDrawable(null);
        backgroundImagePath = "";
        setBgImageSize(10f);
        setImagebIsBackground(true);
        setCameraZoom(1.4285715f);
        setCameraPos(0, 0);
        unregisterDragPoints();
    }
}
