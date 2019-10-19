package com.rockbite.tools.talos.editor.widgets.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.StringBuilder;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.wrappers.ModuleWrapper;
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

    private Label countLbl;

    private PreviewImageControllerWidget previewController;

    private Image previewImage = new Image();

    private String countStr = "count: ";
    private StringBuilder stringBuilder = new StringBuilder();

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
        add(countLbl).left().top().padLeft(5);
        row();
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

        final ParticleEffectInstance particleEffect = TalosMain.Instance().Project().getParticleEffect();
        particleEffect.update(Gdx.graphics.getDeltaTime());

        int count = particleEffect.getParticleCount();
        stringBuilder.clear();
        stringBuilder.append(countStr).append(count);
        countLbl.setText(stringBuilder.toString());
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

        final ParticleEffectInstance particleEffect = TalosMain.Instance().Project().getParticleEffect();
        particleEffect.render(particleRenderer);

        if (!previewController.isBackground()) {
            previewImage.draw(batch, parentAlpha);
        }
    }

}
