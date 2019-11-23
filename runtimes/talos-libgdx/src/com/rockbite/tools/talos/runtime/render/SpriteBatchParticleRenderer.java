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

package com.rockbite.tools.talos.runtime.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.rockbite.tools.talos.runtime.Particle;
import com.rockbite.tools.talos.runtime.ParticleEffectInstance;
import com.rockbite.tools.talos.runtime.ParticleEmitterInstance;

public class SpriteBatchParticleRenderer implements ParticleRenderer {

	private Batch batch;

	Color color = new Color(Color.WHITE);

	public SpriteBatchParticleRenderer () {

	}

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
			if(particleEmitter.isAdditive) {
				batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
			} else {
				batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			}
 			for (int j = 0; j < particleEmitter.activeParticles.size; j++) {
				renderParticle(batch, particleEmitter.activeParticles.get(j));
			}
		}

		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
	}

	private void renderParticle (Batch batch, Particle particle) {
		color.set(particle.color);
		color.mul(particle.particleEmitter.tint);
		color.a = particle.transparency;
		batch.setColor(color);

		if (particle.drawable != null) {
			particle.drawable.setSeed(particle.seed);
			particle.drawable.draw(batch, particle, color);
		}
	}
}
