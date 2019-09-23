package com.rockbite.tools.talos.editor.widgets.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.runtime.ParticleEffectInstance;
import com.rockbite.tools.talos.runtime.render.ParticleRenderer;
import com.rockbite.tools.talos.runtime.render.SpriteBatchParticleRenderer;

public class PreviewWidget extends ViewportWidget {

    Vector2 mid = new Vector2();

    private ParticleRenderer particleRenderer;

    private SpriteBatchParticleRenderer spriteBatchParticleRenderer;

    private ShapeRenderer shapeRenderer;

    private Color green = new Color(Color.GREEN);
    private Color red = new Color(Color.RED);

    public PreviewWidget() {
        super();
        spriteBatchParticleRenderer = new SpriteBatchParticleRenderer(null);
        particleRenderer = spriteBatchParticleRenderer;
        shapeRenderer = new ShapeRenderer();
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

        green.a = 0.2f;
        red.a = 0.2f;
        Gdx.gl.glLineWidth(1f);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(green);
        shapeRenderer.line(-100, 0, 100, 0);
        shapeRenderer.setColor(red);
        shapeRenderer.line(0, -100, 0, 100);
        shapeRenderer.end();


        batch.begin();

        mid.set(0, 0);


        spriteBatchParticleRenderer.setBatch(batch);

        final ParticleEffectInstance particleEffect = TalosMain.Instance().Project().getParticleEffect();
        particleEffect.render(particleRenderer);
    }

}
