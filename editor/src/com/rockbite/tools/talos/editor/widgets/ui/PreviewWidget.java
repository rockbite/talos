package com.rockbite.tools.talos.editor.widgets.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.runtime.ParticleEffectInstance;
import com.rockbite.tools.talos.runtime.render.ParticleRenderer;
import com.rockbite.tools.talos.runtime.render.SpriteBatchParticleRenderer;

public class PreviewWidget extends ViewportWidget {

    Vector2 mid = new Vector2();

    private ParticleRenderer particleRenderer;

    private SpriteBatchParticleRenderer spriteBatchParticleRenderer;

    private ShapeRenderer shapeRenderer;

    private Color tmpColor = new Color();

    public PreviewWidget() {
        super();
        spriteBatchParticleRenderer = new SpriteBatchParticleRenderer(null);
        particleRenderer = spriteBatchParticleRenderer;
        shapeRenderer = new ShapeRenderer();

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
    public void act(float delta) {
        super.act(delta);

        final ParticleEffectInstance particleEffect = TalosMain.Instance().Project().getParticleEffect();
        particleEffect.update(Gdx.graphics.getDeltaTime());
    }

    @Override
    public void drawContent(Batch batch, float parentAlpha) {

        batch.end();

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

        spriteBatchParticleRenderer.setBatch(batch);

        final ParticleEffectInstance particleEffect = TalosMain.Instance().Project().getParticleEffect();
        particleEffect.render(particleRenderer);
    }

}
