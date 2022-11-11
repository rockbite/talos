package com.talosvfx.talos.runtime.render;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.talosvfx.talos.runtime.IEmitter;
import com.talosvfx.talos.runtime.ParticleEffectInstance;
import com.talosvfx.talos.runtime.ParticlePointData;
import com.talosvfx.talos.runtime.ParticlePointGroup;
import com.talosvfx.talos.runtime.ScopePayload;
import com.talosvfx.talos.runtime.modules.DrawableModule;
import com.talosvfx.talos.runtime.modules.MaterialModule;
import com.talosvfx.talos.runtime.modules.MeshGeneratorModule;
import com.talosvfx.talos.runtime.modules.ParticlePointDataGeneratorModule;
import com.talosvfx.talos.runtime.modules.SpriteMaterialModule;
import com.talosvfx.talos.runtime.render.p3d.Simple3DBatch;
import com.talosvfx.talos.runtime.values.DrawableValue;
import lombok.Getter;

public class Particle3DRenderer implements ParticleRenderer {

	@Getter
	private Simple3DBatch batch;

	private PerspectiveCamera worldCamera;

	public Particle3DRenderer (PerspectiveCamera worldCamera) {
		this.worldCamera = worldCamera;
		batch = new Simple3DBatch(4000, new VertexAttributes(VertexAttribute.Position(), VertexAttribute.ColorPacked(), VertexAttribute.TexCoords(0)));

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
			DrawableValue drawableValue = ((SpriteMaterialModule)materialModule).getDrawableValue();
			TextureRegion textureRegion = drawableValue.getDrawable().getTextureRegion();

			batch.render(verts, textureRegion.getTexture());
		}
	}

	@Override
	public void render (float[] verts, int vertCount, short[] tris, int triCount, MaterialModule materialModule) {
		if (materialModule instanceof SpriteMaterialModule) {
			DrawableValue drawableValue = ((SpriteMaterialModule)materialModule).getDrawableValue();
			TextureRegion textureRegion = drawableValue.getDrawable().getTextureRegion();

			batch.render(verts, vertCount, tris, triCount, textureRegion.getTexture());
		}
	}

}
