package com.rockbite.tools.talos.editor.widgets.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.IndexData;
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
import com.badlogic.gdx.utils.PerformanceCounter;
import com.badlogic.gdx.utils.StringBuilder;
import com.badlogic.gdx.utils.TimeUtils;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.wrappers.ModuleWrapper;
import com.rockbite.tools.talos.runtime.ParticleEffectInstance;
import com.rockbite.tools.talos.runtime.ScopePayload;
import com.rockbite.tools.talos.runtime.render.ParticleRenderer;
import com.rockbite.tools.talos.runtime.render.SpriteBatchParticleRenderer;

import java.lang.reflect.Field;
import java.util.Hashtable;

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

    public PreviewWidget() {
        super();
        spriteBatchParticleRenderer = new SpriteBatchParticleRenderer(null);
        particleRenderer = spriteBatchParticleRenderer;
        shapeRenderer = new ShapeRenderer();
        previewController = new PreviewImageControllerWidget(TalosMain.Instance().getSkin()) {
            @Override
            public void removeImage () {
                super.removeImage();
                previewImage.setDrawable(null);
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

        addListener(new InputListener() {

            boolean moving = false;
            Vector2 vec2 = new Vector2();
            Vector3 vec3 = new Vector3();

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                moving = false;
                if(button == 1) {
                    moving = true;
                    return true;
                }
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                super.touchDragged(event, x, y, pointer);
                if(moving) {
                    vec2.set(x, y);
                    localToScreenCoordinates(vec2); // oh shit... not this again
                    vec3.set(vec2.x, vec2.y, 0);
                    camera.unproject(vec3);

                    final ParticleEffectInstance particleEffect = TalosMain.Instance().Project().getParticleEffect();
                    particleEffect.setPosition(vec3.x, vec3.y);
                }
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
            }
        });
    }

    @Override
    protected void cameraScrolledWithAmount (int amount) {
        super.cameraScrolledWithAmount(amount);
        previewController.setFieldOfWidth(camera.zoom * camera.viewportWidth);
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
                TalosMain.Instance().Project().getProjectAssetProvider().addTextureAsTextureRegion(fileHandle.nameWithoutExtension(), texture);
                final TextureRegion textureRegion = new TextureRegion(texture);
                previewImage.setDrawable(new TextureRegionDrawable(textureRegion));
                previewController.setImageWidth(10);
            }
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        long timeBefore = TimeUtils.nanoTime();
        final ParticleEffectInstance particleEffect = TalosMain.Instance().Project().getParticleEffect();
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

        float rt = renderTime.average/1000000f;
        float cp = cpuTime.average/1000000f;

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

        camera.zoom = previewController.getPreviewBoxWidth() / camera.viewportWidth;

        tmpColor.set(Color.WHITE);
        tmpColor.a = 0.2f;
        Gdx.gl.glLineWidth(1f);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(tmpColor);
        shapeRenderer.line(-100, 0, 100, 0);
        shapeRenderer.setColor(tmpColor);
        shapeRenderer.line(0, -100, 0, 100);
        shapeRenderer.end();

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

        final ParticleEffectInstance particleEffect = TalosMain.Instance().Project().getParticleEffect();
        particleEffect.render(particleRenderer);

        batch.flush();
        renderTime.put(TimeUtils.timeSinceNanos(timeBefore));
        trisCount = (int) (glProfiler.getVertexCount().average / 3f);
        glProfiler.disable();


        if (!previewController.isBackground()) {
            previewImage.draw(batch, parentAlpha);
        }
    }

    public GLProfiler getGLProfiler() {
        return glProfiler;
    }
}
