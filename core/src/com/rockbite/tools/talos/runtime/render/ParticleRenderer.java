package com.rockbite.tools.talos.runtime.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.rockbite.tools.talos.runtime.Particle;
import com.rockbite.tools.talos.runtime.ParticleEmitter;
import com.rockbite.tools.talos.runtime.ParticleSystem;

public class ParticleRenderer {

    ParticleSystem particleSystem;

    Color color = new Color(Color.WHITE);

    private Vector2 position = new Vector2();

    public ParticleRenderer() {

    }

    public ParticleRenderer(ParticleSystem particleSystem) {
        this.particleSystem = particleSystem;
    }

    public void render(Batch batch) {
        if(particleSystem == null) return;

        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

        for(int ef = 0; ef < particleSystem.getEffectInstances().size; ef++) {
            for(int i = 0; i < particleSystem.getEffectInstances().get(ef).getEmitters().size; i++) {
                Array<ParticleEmitter> emitters = particleSystem.getEffectInstances().get(ef).getEmitters();
                for(int j = 0; j < emitters.get(i).activeParticles.size; j++) {
                    renderParticle(batch, emitters.get(i).activeParticles.get(j));
                }
            }
        }



        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    private void renderParticle(Batch batch, Particle particle) {
        color.set(particle.color);
        color.a = particle.transparency;
        float rotation = particle.rotation * 360f;
        float size = particle.size;
        batch.setColor(color);

        if(particle.drawable != null) {
            particle.drawable.draw(batch,
                    position.x + particle.position.x-size/2f,
                    position.y + particle.position.y-size/2f,
                    size/2f,
                    size/2f,
                    size,
                    size,
                    rotation);
        }
    }

    public void dispose() {

    }

    public void setParticleSystem(ParticleSystem particleSystem) {
        this.particleSystem = particleSystem;
    }

    public ParticleSystem getParticleSystem() {
        return particleSystem;
    }

    public void setPosition(float x, float y) {
        position.set(x, y);
    }
}
