package com.talosvfx.talos.runtime.scene.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonBatch;
import com.badlogic.gdx.utils.ObjectMap;
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

	public SimpleParticleComponentRenderer (GameObjectRenderer gameObjectRenderer) {
		super(gameObjectRenderer);
		renderer = new SpriteBatchParticleRenderer();
	}

	@Override
	public void render (Batch batch, Camera camera, GameObject gameObject, ParticleComponent<?> rendererComponent) {
		renderer.setCamera(camera);
		renderer.setBatch((PolygonBatch)batch);

		TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);
		ParticleComponent<T> particleComponent = gameObject.getComponent(ParticleComponent.class);

		T resource = particleComponent.gameAsset.getResource();
		ParticleEffectDescriptor descriptor = resource.getDescriptorSupplier().get();

		if (descriptor == null) return;

		ParticleEffectInstance instance = obtainParticle(gameObject, descriptor);
		instance.setPosition(transformComponent.worldPosition.x, transformComponent.worldPosition.y, 0);
		if (!gameObjectRenderer.isSkipUpdates()) {
			instance.update(Gdx.graphics.getDeltaTime());
		}

		renderer.render(instance);
	}

	//todo needs to be done properly for runtime
	private static final ObjectMap<ParticleComponent, ParticleEffectInstance> particleCache = new ObjectMap<>();

	private ParticleEffectInstance obtainParticle (GameObject gameObject, ParticleEffectDescriptor descriptor) {
		ParticleComponent component = gameObject.getComponent(ParticleComponent.class);

		if(particleCache.containsKey(component)) {
			ParticleEffectInstance particleEffectInstance = particleCache.get(component);
			component.setEffectRef(particleEffectInstance);
			return particleEffectInstance;
		} else {
			ParticleEffectInstance instance = descriptor.createEffectInstance();
			component.setEffectRef(instance);
			particleCache.put(component, instance);
			return instance;
		}
	}
}
