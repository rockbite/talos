package com.talosvfx.talos.runtime.vfx.render;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.talosvfx.talos.runtime.vfx.IEmitter;
import com.talosvfx.talos.runtime.vfx.ParticleEffectInstance;
import com.talosvfx.talos.runtime.vfx.ScopePayload;
import com.talosvfx.talos.runtime.vfx.modules.DrawableModule;
import com.talosvfx.talos.runtime.vfx.modules.MaterialModule;
import com.talosvfx.talos.runtime.vfx.modules.MeshGeneratorModule;
import com.talosvfx.talos.runtime.vfx.modules.ParticlePointDataGeneratorModule;
import com.talosvfx.talos.runtime.vfx.modules.SpriteMaterialModule;
import com.talosvfx.talos.runtime.vfx.render.p3d.Simple3DBatch;
import lombok.Getter;

public class Particle3DRenderer implements ParticleRenderer {

	@Getter
	private Simple3DBatch batch;

	private Camera worldCamera;

	public Particle3DRenderer (Camera worldCamera, Simple3DBatch simple3DBatch) {
		this.worldCamera = worldCamera;
		this.batch = simple3DBatch;
	}

	public Particle3DRenderer (Camera worldCamera) {
		this(worldCamera, new Simple3DBatch(4000, new VertexAttributes(VertexAttribute.Position(), VertexAttribute.ColorPacked(), VertexAttribute.TexCoords(0))));
	}

	@Override
	public Camera getCamera () {
		return worldCamera;
	}

	@Override
	public void render (ParticleEffectInstance particleEffectInstance) {

		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

		for (int i = 0; i < particleEffectInstance.getEmitters().size; i++) {
			final IEmitter particleEmitter = particleEffectInstance.getEmitters().get(i);
			if (!particleEmitter.isVisible())
				continue;
			if (particleEmitter.isBlendAdd()) {
				batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
			} else {
				if (particleEmitter.isAdditive()) {
					batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
				} else {
					batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
				}
			}

			final DrawableModule drawableModule = particleEmitter.getDrawableModule();
			if (drawableModule == null)
				continue;

			MeshGeneratorModule meshGenerator = drawableModule.getMeshGenerator();
			if (meshGenerator == null)
				continue;
			meshGenerator.setRenderMode(true);

			if (drawableModule == null)
				continue;
			if (drawableModule.getMaterialModule() == null)
				continue;
			ParticlePointDataGeneratorModule particlePointDataGeneratorModule = drawableModule.getPointDataGenerator();
			if (particlePointDataGeneratorModule == null)
				continue;

			int cachedMode = particleEmitter.getScope().getRequestMode();
			int cachedRequesterID = particleEmitter.getScope().getRequesterID();

			particleEmitter.getScope().setCurrentRequestMode(ScopePayload.SUB_PARTICLE_ALPHA);

			meshGenerator.render(this, drawableModule.getMaterialModule(), particlePointDataGeneratorModule.pointData);


			particleEmitter.getScope().setCurrentRequestMode(cachedMode);
			particleEmitter.getScope().setCurrentRequesterID(cachedRequesterID);
		}

		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

	}

	@Override
	public void render (float[] verts, MaterialModule materialModule) {
		if (materialModule instanceof SpriteMaterialModule) {
			TextureRegion textureRegion = ((SpriteMaterialModule)materialModule).getTextureRegion();

			batch.render(verts, textureRegion.getTexture());
		}
	}

	@Override
	public void render (float[] verts, int vertCount, short[] tris, int triCount, MaterialModule materialModule) {
		if (materialModule instanceof SpriteMaterialModule) {
			TextureRegion textureRegion = ((SpriteMaterialModule)materialModule).getTextureRegion();

			batch.render(verts, vertCount, tris, triCount, textureRegion.getTexture());
		}
	}

}
