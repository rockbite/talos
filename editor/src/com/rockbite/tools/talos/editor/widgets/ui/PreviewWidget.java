package com.rockbite.tools.talos.editor.widgets.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.runtime.render.ParticleRenderer;

public class PreviewWidget extends ViewportWidget {

    Vector2 mid = new Vector2();

    private ParticleRenderer particleRenderer;

    private ShapeRenderer shapeRenderer;

    private Color green = new Color(Color.GREEN);
    private Color red = new Color(Color.RED);

    public PreviewWidget() {
        super();
        particleRenderer = new ParticleRenderer();
        shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        TalosMain.Instance().Project().getParticleSystem().update(delta);
        particleRenderer.setParticleSystem(TalosMain.Instance().Project().getParticleSystem());
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

        particleRenderer.render(batch);
        particleRenderer.setPosition(mid.x, mid.y);
    }

}
