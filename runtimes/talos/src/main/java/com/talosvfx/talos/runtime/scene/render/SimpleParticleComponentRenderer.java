package com.talosvfx.talos.runtime.scene.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonBatch;
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.spine.SkeletonData;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.GameObjectRenderer;
import com.talosvfx.talos.runtime.scene.components.ParticleComponent;
import com.talosvfx.talos.runtime.scene.components.TransformComponent;
import com.talosvfx.talos.runtime.vfx.ParticleEffectDescriptor;
import com.talosvfx.talos.runtime.vfx.ParticleEffectInstance;
import com.talosvfx.talos.runtime.vfx.render.SpriteBatchParticleRenderer;
import com.talosvfx.talos.runtime.vfx.serialization.BaseVFXProjectData;

public class SimpleParticleComponentRenderer<T extends BaseVFXProjectData> extends ComponentRenderer<ParticleComponent<?>> {

	private final SpriteBatchParticleRenderer renderer;
	private float delta;

	public SimpleParticleComponentRenderer (GameObjectRenderer gameObjectRenderer) {
		super(gameObjectRenderer);
		renderer = new SpriteBatchParticleRenderer();
	}

	@Override
	public void update (GameObject gameObject, ParticleComponent<?> component, float delta) {
		super.update(gameObject, component, delta);
		this.delta = delta;
	}

	@Override
	public void render (Batch batch, Camera camera, GameObject gameObject, ParticleComponent<?> rendererComponent) {
		renderer.setCamera(camera);
		renderer.setBatch((PolygonBatch)batch);

		TransformComponent transformComponent = gameObject.getTransformComponent();
		ParticleComponent<T> particleComponent = gameObject.getParticleComponent();

		GameAsset<T> gameAsset = particleComponent.gameAsset;

		if (gameAsset.isBroken()) {
			GameObjectRenderer.renderBrokenComponent(batch, gameObject, transformComponent);
			return;
		}

		T resource = gameAsset.getResource();



		ParticleEffectDescriptor descriptor = resource.getDescriptorSupplier().get();

		if (descriptor == null) return;

		ParticleEffectInstance instance = particleComponent.getEffectRef();
		if (instance == null) {
			instance = descriptor.createEffectInstance();
			particleComponent.setEffectRef(instance);
		}
		instance.setWorldRotation(transformComponent.worldRotation);
		instance.setWorldScale(transformComponent.worldScale);
		instance.setPosition(transformComponent.worldPosition.x, transformComponent.worldPosition.y, 0);
		if (!gameObjectRenderer.isSkipUpdates()) {
			instance.update(delta);
		}

		renderer.render(instance);
	}

}
