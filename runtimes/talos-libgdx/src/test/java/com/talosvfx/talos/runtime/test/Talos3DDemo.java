package com.talosvfx.talos.runtime.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.rockbite.bongo.engine.render.AutoReloadingShaderProgram;
import com.rockbite.bongo.engine.render.ShaderFlags;
import com.rockbite.bongo.engine.render.ShaderSourceProvider;
import com.rockbite.bongo.engine.render.SpriteShaderCompiler;
import com.talosvfx.talos.runtime.vfx.ParticleEffectDescriptor;
import com.talosvfx.talos.runtime.vfx.ParticleEffectInstance;
import com.talosvfx.talos.runtime.assets.AtlasAssetProvider;
import com.talosvfx.talos.runtime.vfx.render.Particle3DRenderer;
import com.talosvfx.talos.runtime.vfx.render.p3d.Simple3DBatch;

public class Talos3DDemo extends ApplicationAdapter {

	private PerspectiveCamera camera;
	private ParticleEffectInstance effect;

	private Particle3DRenderer defaultRenderer;

	private AutoReloadingShaderProgram shaderProgram;
	private FirstPersonCameraController firstPersonCameraController;

	private ShapeRenderer shapeRenderer;

	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setWindowedMode(1280, 720);
		config.setTitle("Talos 3D Demo");
		Lwjgl3Application application = new Lwjgl3Application(new Talos3DDemo(), config);
	}

	@Override
	public void create () {
		camera = new PerspectiveCamera(70, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.near = 0.01f;
		camera.far = 200;

		firstPersonCameraController = new FirstPersonCameraController(camera);
		Gdx.input.setInputProcessor(firstPersonCameraController);

		TextureRegion fireRegion = new TextureRegion(new Texture(Gdx.files.internal("fire.png")));
		TextureRegion spotRegion = new TextureRegion(new Texture(Gdx.files.internal("spot.png")));
		TextureAtlas textureAtlas = new TextureAtlas();
		textureAtlas.addRegion("fire", fireRegion);
		textureAtlas.addRegion("spot", spotRegion);

		AtlasAssetProvider atlasAssetProvider = new AtlasAssetProvider(textureAtlas);

		/**
		 * Creating particle effect instance from particle effect descriptor
		 */
		ParticleEffectDescriptor effectDescriptor = new ParticleEffectDescriptor(Gdx.files.internal("test.p"), atlasAssetProvider);
		effect = effectDescriptor.createEffectInstance();

		defaultRenderer = new Particle3DRenderer(camera);

		shaderProgram = new AutoReloadingShaderProgram(ShaderSourceProvider.resolveVertex("core/particle", Files.FileType.Classpath), ShaderSourceProvider.resolveFragment("core/particle", Files.FileType.Classpath));

		String shapeVertexSource = ShaderSourceProvider.resolveVertex("core/shape", Files.FileType.Classpath).readString();
		String shapeFragmentSource = ShaderSourceProvider.resolveFragment("core/shape", Files.FileType.Classpath).readString();

		shapeRenderer = new ShapeRenderer(5000, SpriteShaderCompiler.getOrCreateShader("core/shape", shapeVertexSource, shapeFragmentSource, new ShaderFlags()));

	}

	@Override
	public void render () {
		//update
		float delta = Math.min(Gdx.graphics.getDeltaTime(), 1/30f);
		effect.update(delta);

		firstPersonCameraController.update(Gdx.graphics.getDeltaTime());

		// now to render
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		Gdx.gl.glEnable(GL20.GL_BLEND);



		final Simple3DBatch batch = defaultRenderer.getBatch();
		batch.begin(camera, shaderProgram.getShaderProgram());
		effect.render(defaultRenderer);
		batch.end();

		shapeRenderer.setProjectionMatrix(camera.combined);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		shapeRenderer.setColor(Color.RED);
		shapeRenderer.line(-10, 0, 0, 10, 0, 0);
		shapeRenderer.setColor(Color.GREEN);
		shapeRenderer.line(0, -10, 0, 0, 10, 0);
		shapeRenderer.setColor(Color.BLUE);
		shapeRenderer.line(0, 0, -10, 0, 0, 10);
		shapeRenderer.end();

	}

	@Override
	public void resize (int width, int height) {
	}

	@Override
	public void dispose () {

	}
}
