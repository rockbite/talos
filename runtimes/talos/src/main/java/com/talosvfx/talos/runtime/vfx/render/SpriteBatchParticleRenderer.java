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

package com.talosvfx.talos.runtime.vfx.render;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.runtime.vfx.IEmitter;
import com.talosvfx.talos.runtime.vfx.Particle;
import com.talosvfx.talos.runtime.vfx.ParticleEffectInstance;
import com.talosvfx.talos.runtime.vfx.ParticlePointData;
import com.talosvfx.talos.runtime.vfx.ParticlePointGroup;
import com.talosvfx.talos.runtime.vfx.ScopePayload;
import com.talosvfx.talos.runtime.vfx.modules.DrawableModule;
import com.talosvfx.talos.runtime.vfx.modules.MaterialModule;
import com.talosvfx.talos.runtime.vfx.modules.MeshGeneratorModule;
import com.talosvfx.talos.runtime.vfx.modules.ParticlePointDataGeneratorModule;
import com.talosvfx.talos.runtime.vfx.modules.SpriteMaterialModule;
import com.talosvfx.talos.runtime.vfx.utils.DefaultShaders;

public class SpriteBatchParticleRenderer implements ParticleRenderer {

	public PolygonBatch batch;

	Color color = new Color(Color.WHITE);
	private ShaderProgram blendAddShader;
	private Camera camera;

	public SpriteBatchParticleRenderer () {
		initShaders();
	}

	public SpriteBatchParticleRenderer (Camera camera) {
		this();
		this.camera = camera;
	}

	public SpriteBatchParticleRenderer (Camera camera, PolygonBatch batch) {
		this(camera);
		this.batch = batch;
	}

	private void initShaders() {
		blendAddShader = new ShaderProgram(
				DefaultShaders.DEFAULT_VERTEX_SHADER,
				DefaultShaders.BLEND_ADD_FRAGMENT_SHADER);
	}

	public void setBatch (PolygonBatch batch) {
		this.batch = batch;
	}

	@Override
	public Camera getCamera () {
		return camera;
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

			final DrawableModule drawableModule = particleEmitter.getDrawableModule();
			if (drawableModule == null) continue;

			MeshGeneratorModule meshGenerator = drawableModule.getMeshGenerator();
			if (meshGenerator == null) continue;
			meshGenerator.setRenderMode(false);

			if (drawableModule == null) continue;
			if (drawableModule.getMaterialModule() == null) continue;
			ParticlePointDataGeneratorModule particlePointDataGeneratorModule = drawableModule.getPointDataGenerator();
			if (particlePointDataGeneratorModule == null) continue;

			int cachedMode = particleEmitter.getScope().getRequestMode();
			int cachedRequesterID = particleEmitter.getScope().getRequesterID();

			particleEmitter.getScope().setCurrentRequestMode(ScopePayload.SUB_PARTICLE_ALPHA);
			meshGenerator.render(this, drawableModule.getMaterialModule(), particleEmitter.pointData());

			particleEmitter.getScope().setCurrentRequestMode(cachedMode);
			particleEmitter.getScope().setCurrentRequesterID(cachedRequesterID);


 			if (batch.getShader() != prevShader) {
 				batch.setShader(prevShader);
			}
		}

		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
	}

	@Override
	public void render (float[] verts, MaterialModule materialModule) {
		if (materialModule instanceof SpriteMaterialModule) {


		}
	}

	@Override
	public void render (float[] verts, int vertCount, short[] tris, int triCount, MaterialModule materialModule) {
		if (materialModule instanceof SpriteMaterialModule) {
			TextureRegion textureRegion = ((SpriteMaterialModule)materialModule).getTextureRegion();

			final Texture texture = textureRegion.getTexture();

			batch.draw(texture, verts, 0, vertCount, tris, 0, triCount);
		}
	}

	private void renderParticle (Batch batch, Particle particle, float parentAlpha) {
		color.set(Color.WHITE);
		batch.setColor(color);



	}

	public void setCamera(Camera camera) {
		this.camera = camera;
	}
}
