package com.rockbite.tools.talos.editor.widgets.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.rockbite.tools.talos.runtime.ParticleRenderer;
import com.rockbite.tools.talos.runtime.ParticleSystem;

public class PreviewWidget extends ViewportWidget {


    Vector2 mid = new Vector2();
    private ParticleSystem particleSystem;

    private ParticleRenderer particleRenderer;

    public PreviewWidget() {

        particleRenderer = new ParticleRenderer();
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        particleSystem.update(delta);
    }

    @Override
    public void drawContent(Batch batch, float parentAlpha) {

        mid.x = -getStage().getWidth()/2f + getRight();
        mid.y = 50;

        particleRenderer.render(batch);
        particleRenderer.setPosition(mid.x, mid.y);
    }

    public void setParticleSystem(ParticleSystem particleSystem) {
        this.particleSystem = particleSystem;
        particleRenderer.setParticleSystem(particleSystem);
    }

    public ParticleSystem getParticleSystem() {
        return particleSystem;
    }
}
