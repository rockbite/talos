package com.talosvfx.talos;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.rockbite.bongo.engine.systems.RenderPassSystem;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.assets.TalosAssetProvider;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.events.ProjectLoadedEvent;
import com.talosvfx.talos.editor.project2.ProjectSplash;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.project2.TalosLocalPrefs;
import com.talosvfx.talos.editor.project2.TalosProjectData;
import com.talosvfx.talos.editor.project2.TalosVFXUtils;
import com.talosvfx.talos.editor.widgets.ui.FilteredTree;
import com.talosvfx.talos.editor.widgets.ui.ModuleListPopup;
import com.talosvfx.talos.editor.wrappers.EmitterModuleWrapper;
import com.talosvfx.talos.editor.wrappers.WrapperRegistry;
import com.talosvfx.talos.runtime.modules.EmitterModule;
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

		AssetRepository.init();
		SharedResources.projectLoader = this::projectLoader;
		TalosVFXUtils.talosAssetProvider = new TalosAssetProvider();

		TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("skin/uiskin.atlas"));
		skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
		skin.addRegions(atlas);

		VisUI.load(skin);
		SharedResources.skin = skin;

		TalosVFXUtils.init();

		stage = new Stage(new ScreenViewport(), new PolygonSpriteBatch());

		SharedResources.stage = stage;

		layoutGridContainer = new Table();

		Table fullScreen = new Table();
		fullScreen.setFillParent(true);

		Table topBar = new Table();
		fullScreen.add(topBar).growX().height(64);
		fullScreen.row();

		fullScreen.add(layoutGridContainer).grow();

		stage.addActor(fullScreen);

		Gdx.input.setInputProcessor(stage);

		openProjectExplorer();
	}

	private void projectLoader (TalosProjectData projectData) {
		SharedResources.currentProject = projectData;

		TalosLocalPrefs.Instance().updateProject(projectData);

		layoutGridContainer.clearChildren();
		layoutGridContainer.add(projectData.getLayoutGrid()).grow();

		ProjectLoadedEvent projectLoadedEvent = Notifications.obtainEvent(ProjectLoadedEvent.class);
		projectLoadedEvent.setProjectData(projectData);
		Notifications.fireEvent(projectLoadedEvent);

	}


	private void openProjectExplorer () {
		ProjectSplash projectSplash = new ProjectSplash("Projects");
		projectSplash.show(stage);
	}

	@Override
	public void render () {
		super.render();
		Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1f);
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

	@Override
	public void dispose () {
		super.dispose();
		skin.dispose();
		stage.dispose();
		VisUI.dispose();
	}
}
