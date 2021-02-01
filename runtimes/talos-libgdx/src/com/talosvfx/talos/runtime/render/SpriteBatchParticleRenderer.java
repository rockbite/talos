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

package com.talosvfx.talos.runtime.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.talosvfx.talos.runtime.IEmitter;
import com.talosvfx.talos.runtime.Particle;
import com.talosvfx.talos.runtime.ParticleEffectInstance;
import com.talosvfx.talos.runtime.utils.DefaultShaders;

public class SpriteBatchParticleRenderer implements ParticleRenderer {

	private Batch batch;

	Color color = new Color(Color.WHITE);
	private ShaderProgram blendAddShader;

	public SpriteBatchParticleRenderer () {
		initShaders();
	}

	public SpriteBatchParticleRenderer (Batch batch) {
		this.batch = batch;
		initShaders();
	}

	private void initShaders() {
		blendAddShader = new ShaderProgram(
				DefaultShaders.DEFAULT_VERTEX_SHADER,
				DefaultShaders.BLEND_ADD_FRAGMENT_SHADER);
	}

	public void setBatch (Batch batch) {
		this.batch = batch;
	}

	@Override
	public void render (ParticleEffectInstance particleEffectInstance) {
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

		for (int i = 0; i < particleEffectInstance.getEmitters().size; i++) {
			final IEmitter particleEmitter = particleEffectInstance.getEmitters().get(i);
			if(!particleEmitter.isVisible()) continue;
			if(particleEmitter.isBlendAdd()) {
				batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
			} else {
				if (particleEmitter.isAdditive()) {
					batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
				} else {
					batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
				}
			}
			ShaderProgram prevShader = batch.getShader();
			if (particleEmitter.isBlendAdd() && prevShader != blendAddShader) {
				//batch.setShader(blendAddShader); //TODO: let's leave any shader stuff to shader graph, and rest can be baked
			}
 			for (int j = 0; j < particleEmitter.getActiveParticleCount(); j++) {
				renderParticle(batch, particleEmitter.getActiveParticles().get(j), particleEffectInstance.alpha);
			}
 			if(batch.getShader() != prevShader) {
 				batch.setShader(prevShader);
			}
		}

		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
	}

	private void renderParticle (Batch batch, Particle particle, float parentAlpha) {
		color.set(particle.color);
		color.mul(particle.getEmitter().getTint());
		color.a = particle.transparency * parentAlpha;
		batch.setColor(color);

		if (particle.drawable != null) {
			particle.drawable.setCurrentParticle(particle);
			particle.drawable.draw(batch, particle, color);
		}
	}
}
