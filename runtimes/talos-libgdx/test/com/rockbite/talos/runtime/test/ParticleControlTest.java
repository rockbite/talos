package com.rockbite.talos.runtime.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.rockbite.talos.runtime.test.utils.CameraController;
import com.rockbite.tools.talos.runtime.ParticleEffectDescriptor;
import com.rockbite.tools.talos.runtime.ParticleEffectInstance;
import com.rockbite.tools.talos.runtime.render.ParticleRenderer;
import com.rockbite.tools.talos.runtime.render.SpriteBatchParticleRenderer;

public class ParticleControlTest extends ApplicationAdapter {

	private OrthographicCamera orthographicCamera;
	private ParticleRenderer particleRenderer;
	private ShapeRenderer shapeRenderer;
	private SpriteBatch batch;

	private CameraController cameraController;

	private ParticleEffectInstance particleEffectInstance;

	@Override
	public void create () {

		orthographicCamera = new OrthographicCamera();
		float width = 10f;
		float aspect = (float)Gdx.graphics.getWidth()/(float)Gdx.graphics.getHeight();
		orthographicCamera.setToOrtho(false, width, width / aspect);
		shapeRenderer = new ShapeRenderer();

		batch = new SpriteBatch();

		particleRenderer = new SpriteBatchParticleRenderer(batch);

		cameraController = new CameraController(orthographicCamera);
		Gdx.input.setInputProcessor(cameraController);

		ParticleEffectDescriptor descriptor = new ParticleEffectDescriptor();

		TextureAtlas atlas = new TextureAtlas();
		atlas.addRegion("fire", new TextureRegion(new TextureRegion(new Texture(Gdx.files.internal("fire.png")))));
		descriptor.setTextureAtlas(atlas);
		descriptor.load(Gdx.files.internal("testfire.p"));

		particleEffectInstance = descriptor.createEffectInstance();
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		orthographicCamera.update();

		shapeRenderer.setProjectionMatrix(orthographicCamera.combined);

		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		shapeRenderer.setColor(Color.GREEN);
		shapeRenderer.line(-100, 0, 100, 0);
		shapeRenderer.setColor(Color.RED);
		shapeRenderer.line(0, -100, 0, 100);
		shapeRenderer.end();


		batch.setProjectionMatrix(orthographicCamera.combined);
		batch.begin();

		particleEffectInstance.resume();

		particleEffectInstance.update(Gdx.graphics.getDeltaTime());
		particleRenderer.render(particleEffectInstance);

		batch.end();
	}

	public static void main (String[] args) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1280;
		config.height = 720;
		new LwjglApplication(new ParticleControlTest(), config);
	}
}
