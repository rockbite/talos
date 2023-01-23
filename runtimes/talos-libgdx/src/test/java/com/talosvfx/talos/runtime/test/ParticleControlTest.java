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

package com.talosvfx.talos.runtime.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.talosvfx.talos.runtime.assets.AtlasAssetProvider;
import com.talosvfx.talos.runtime.test.utils.CameraController;
import com.talosvfx.talos.runtime.vfx.ParticleEffectDescriptor;
import com.talosvfx.talos.runtime.vfx.ParticleEffectInstance;
import com.talosvfx.talos.runtime.vfx.render.ParticleRenderer;
import com.talosvfx.talos.runtime.vfx.render.SpriteBatchParticleRenderer;

public class ParticleControlTest extends ApplicationAdapter {

	private OrthographicCamera orthographicCamera;
	private ParticleRenderer particleRenderer;
	private ShapeRenderer shapeRenderer;
	private PolygonSpriteBatch batch;

	private CameraController cameraController;

	private ParticleEffectInstance particleEffectInstance;

	Stage stage;

	@Override
	public void create () {

		orthographicCamera = new OrthographicCamera();
		float width = 20;
		float aspect = (float)Gdx.graphics.getWidth()/(float)Gdx.graphics.getHeight();
		orthographicCamera.setToOrtho(false, width, width / aspect);
		orthographicCamera.position.set(0, 0, 0);
		orthographicCamera.update();
		shapeRenderer = new ShapeRenderer();

		batch = new PolygonSpriteBatch();

		particleRenderer = new SpriteBatchParticleRenderer(orthographicCamera, batch);

		cameraController = new CameraController(orthographicCamera);


		TextureAtlas atlas = new TextureAtlas();
		atlas.addRegion("fire", new TextureRegion(new TextureRegion(new Texture(Gdx.files.internal("fire.png")))));
		atlas.addRegion("spot", new TextureRegion(new TextureRegion(new Texture(Gdx.files.internal("spot.png")))));


		AtlasAssetProvider atlasAssetProvider = new AtlasAssetProvider(atlas);
		ParticleEffectDescriptor descriptor = new ParticleEffectDescriptor(Gdx.files.internal("test.p"), atlasAssetProvider);


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

	@Override
	public void resize (int width, int height) {
		super.resize(width, height);
		stage.getViewport().update(width, height);
	}

	public static void main (String[] args) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setWindowedMode(1280, 720);
		new Lwjgl3Application(new ParticleControlTest(), config);
	}
}
