package com.talosvfx.talos.editor.widgets.ui;

import com.artemis.BaseSystem;
import com.artemis.Entity;
import com.artemis.EntityEdit;
import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.rockbite.bongo.engine.Bongo;
import com.rockbite.bongo.engine.EngineBuilder;
import com.rockbite.bongo.engine.components.render.DepthLayer;
import com.rockbite.bongo.engine.components.render.ShadedLayer;
import com.rockbite.bongo.engine.components.render.ShadowLayer;
import com.rockbite.bongo.engine.components.singletons.Cameras;
import com.rockbite.bongo.engine.gltf.scene.SceneMaterial;
import com.rockbite.bongo.engine.gltf.scene.SceneModel;
import com.rockbite.bongo.engine.gltf.scene.SceneModelInstance;
import com.rockbite.bongo.engine.meshutils.CubeUtils;
import com.rockbite.bongo.engine.systems.CameraControllerSystem;
import com.rockbite.bongo.engine.systems.assets.AssetSystem;
import com.rockbite.bongo.engine.systems.render.DepthPassSystem;
import com.rockbite.bongo.engine.systems.render.EngineDebugSystem;
import com.rockbite.bongo.engine.systems.render.EnvironmentConfigSystem;
import com.rockbite.bongo.engine.systems.render.ShadedPassSystem;
import com.rockbite.bongo.engine.systems.render.ShadowPassSystem;
import com.talosvfx.talos.runtime.ParticleEffectDescriptor;
import com.talosvfx.talos.runtime.ParticleEffectInstance;
import com.talosvfx.talos.runtime.components.Particle;
import com.talosvfx.talos.runtime.systems.render.ParticleRenderPassSystem;

public class BongoPreview {

	private final World world;

	private final int particleEntityID;

	public BongoPreview () {
		Bongo.DEBUG = true;
		Bongo.init();

		ShaderProgram.pedantic = false;

		BaseSystem[] userSystems = {
			new CameraControllerSystem(),
			new EnvironmentConfigSystem(),

			//RENDER
			new DepthPassSystem().setContextStartEnd(true, false),
			new ShadowPassSystem(),
			new ShadedPassSystem().setContextStartEnd(false, true),

			new ParticleRenderPassSystem(),

			new EngineDebugSystem()
		};

		world = EngineBuilder.buildWorld(userSystems);


		//floor
		final SceneModelInstance floor = createBox("Floor", Color.WHITE, 20, 1, 20, 0.5f, 0.2f);
		floor.getTransform().setToTranslation(0, -1f, 0);

		//Box
		SceneModelInstance box = createBox("Box", Color.RED, 1, 1, 1, 0.0f, 0.4f);
		box.getTransform().setToTranslation(3, 1f, 0);


		//Particle
		particleEntityID = world.create();
		EntityEdit edit = world.edit(particleEntityID);
		Particle particle = edit.create(Particle.class);
	}

	public void setCamera (Camera camera) {
		final Cameras cameras = world.getSystem(CameraControllerSystem.class).getCameras();
		cameras.setGameCamera(camera);

	}

	public void updateParticleInstance (ParticleEffectInstance particleEffectInstance) {
		final Entity entity = world.getEntity(particleEntityID);
		entity.getComponent(Particle.class).setParticleEffectInstance(particleEffectInstance);
	}


	private SceneModelInstance createBox (String name, Color color, float width, float height, float depth, float metal, float roughness) {
		final SceneModel testBox = CubeUtils.createBox(
			name,
			width, height, depth,
			SceneMaterial.BasicPBR(name, color, metal, roughness));

		final int entity = world.create();
		final EntityEdit edit = world.edit(entity);
		edit.create(ShadedLayer.class);
		edit.create(DepthLayer.class);
		edit.create(ShadowLayer.class);
//		edit.create(UnlitLayer.class);

		SceneModelInstance modelInstance = new SceneModelInstance(testBox);
		edit.add(modelInstance);
		return modelInstance;
	}

	public void render () {
		world.process();
	}

	public Camera getWorldCamera () {
		return world.getSystem(CameraControllerSystem.class).getCameras().getGameCamera();
	}

	public InputAdapter getCameraController () {
		return world.getSystem(CameraControllerSystem.class).getCameraController();
	}

	public void setCameraController (InputAdapter cameraInputController) {
		world.getSystem(CameraControllerSystem.class).setCameraController(cameraInputController);
	}


}