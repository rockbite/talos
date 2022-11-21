package com.talosvfx.talos;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.rockbite.bongo.engine.systems.RenderPassSystem;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.project2.TalosProjectData;
import lombok.Getter;

import java.util.UUID;

public class TalosMain2 extends ApplicationAdapter {
	@Getter
	private Skin skin;
	private Stage stage;
	private Table layoutGridContainer;

	@Override
	public void create () {
		super.create();


		TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("skin/uiskin.atlas"));
		skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
		skin.addRegions(atlas);

		VisUI.load(skin);

		SharedResources.skin = skin;

		stage = new Stage(new ScreenViewport());

		layoutGridContainer = new Table();

		Table fullScreen = new Table();
		fullScreen.setFillParent(true);

		Table topBar = new Table();
		fullScreen.add(topBar).growX().height(64);
		fullScreen.row();

		fullScreen.add(layoutGridContainer).grow();

		stage.addActor(fullScreen);

		Gdx.input.setInputProcessor(stage);


		//extract it out to preferences, recent, open project ect
		loadTalosProject(new TalosProjectData());
	}

	public void loadTalosProject (TalosProjectData projectData) {
		//Clean up anything from old project

		layoutGridContainer.clearChildren();
		layoutGridContainer.add(projectData.getLayoutGrid()).grow();
	}

	@Override
	public void render () {
		super.render();
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		stage.act();
		stage.draw();
	}

	@Override
	public void resize (int width, int height) {
		super.resize(width, height);

		stage.getViewport().update(width, height, true);
		RenderPassSystem.glViewport.width = width;
		RenderPassSystem.glViewport.height = height;
	}
}
