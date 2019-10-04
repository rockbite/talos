package com.rockbite.tools.talos.runtime.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.rockbite.tools.talos.runtime.Particle;
import com.rockbite.tools.talos.runtime.ParticleEffectInstance;
import com.rockbite.tools.talos.runtime.ParticleEmitterInstance;

public class SpriteBatchParticleRenderer implements ParticleRenderer {

	private Batch batch;

	Color color = new Color(Color.WHITE);

	public SpriteBatchParticleRenderer (Batch batch) {
		this.batch = batch;
	}

	public void setBatch (Batch batch) {
		this.batch = batch;
	}

	@Override
	public void render (ParticleEffectInstance particleEffectInstance) {
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

		for (int i = 0; i < particleEffectInstance.getEmitters().size; i++) {
			final ParticleEmitterInstance particleEmitter = particleEffectInstance.getEmitters().get(i);
			if(!particleEmitter.isVisible) continue;
			for (int j = 0; j < particleEmitter.activeParticles.size; j++) {
				renderParticle(batch, particleEmitter.activeParticles.get(j));
			}
		}

		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
	}

	private void renderParticle (Batch batch, Particle particle) {
		color.set(particle.color);
		color.a = particle.transparency;
		float rotation = particle.rotation;
		batch.setColor(color);

		if (particle.drawable != null) {
			particle.drawable.draw(batch, particle.getX(), particle.getY(), particle.size.x, particle.size.y, rotation);
		}
	}
}
