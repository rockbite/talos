package com.rockbite.talos.runtime.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.rockbite.talos.runtime.test.utils.CameraController;
import com.rockbite.tools.talos.runtime.ParticleEffectDescriptor;
import com.rockbite.tools.talos.runtime.ParticleEffectInstance;
import com.rockbite.tools.talos.runtime.assets.TextureAtlasAssetProvider;
import com.rockbite.tools.talos.runtime.render.ParticleRenderer;
import com.rockbite.tools.talos.runtime.render.SpriteBatchParticleRenderer;

public class ParticleControlTest extends ApplicationAdapter {

	private OrthographicCamera orthographicCamera;
	private ParticleRenderer particleRenderer;
	private ShapeRenderer shapeRenderer;
	private SpriteBatch batch;

	private CameraController cameraController;

	private ParticleEffectInstance particleEffectInstance;

	Stage stage;

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

		ParticleEffectDescriptor descriptor = new ParticleEffectDescriptor();

		TextureAtlas atlas = new TextureAtlas();
		atlas.addRegion("fire", new TextureRegion(new TextureRegion(new Texture(Gdx.files.internal("fire.png")))));
		descriptor.setAssetProvider(new TextureAtlasAssetProvider(atlas));
		descriptor.load(Gdx.files.internal("runtime.p"));

		particleEffectInstance = descriptor.createEffectInstance();

		particleEffectInstance.loopable = true;


		stage = new Stage();

		VisUI.load();

		VisTextButton start = new VisTextButton("Start/Resume");
		VisTextButton pause = new VisTextButton("Pause");
		VisTextButton restart = new VisTextButton("Restart");
		VisTextButton allowCompletion = new VisTextButton("Allow Completion");

		start.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				particleEffectInstance.resume();
			}
		});

		pause.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				particleEffectInstance.pause();
			}
		});

		restart.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				particleEffectInstance.restart();
			}
		});


		allowCompletion.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				particleEffectInstance.allowCompletion();
			}
		});


		Table table = new Table();
		table.setFillParent(true);
		table.defaults().pad(10).top().left();

		table.top().left();

		table.add(start);
		table.row();
		table.add(pause);
		table.row();
		table.add(restart);
		table.row();
		table.add(allowCompletion);

		stage.addActor(table);

		Gdx.input.setInputProcessor(new InputMultiplexer(stage, cameraController));
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

		particleEffectInstance.update(Gdx.graphics.getDeltaTime());
		particleRenderer.render(particleEffectInstance);

		batch.end();

		stage.act();
		stage.draw();
	}


	public static void main (String[] args) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1280;
		config.height = 720;
		new LwjglApplication(new ParticleControlTest(), config);
	}
}
