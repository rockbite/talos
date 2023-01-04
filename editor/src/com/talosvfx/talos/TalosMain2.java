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
import com.rockbite.bongo.engine.render.PolygonSpriteBatchMultiTextureMULTIBIND;
import com.rockbite.bongo.engine.systems.RenderPassSystem;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.assets.TalosAssetProvider;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.commands.CommandsSystem;
import com.talosvfx.talos.editor.notifications.events.FinishInitializingEvent;
import com.talosvfx.talos.editor.notifications.events.ProjectLoadedEvent;
import com.talosvfx.talos.editor.project2.*;
import com.talosvfx.talos.editor.project2.localprefs.TalosLocalPrefs;
import com.talosvfx.talos.editor.project2.input.InputHandling;
import com.talosvfx.talos.editor.project2.savestate.GlobalSaveStateSystem;
import com.talosvfx.talos.editor.project2.savestate.SaveSystem;
import com.talosvfx.talos.editor.socket.SocketServer;
import com.talosvfx.talos.editor.utils.CursorUtil;
import com.talosvfx.talos.editor.widgets.ui.menu.MainMenu;
import lombok.Getter;

public class TalosMain2 extends ApplicationAdapter {
	private final ILauncher launcher;
	@Getter
	private Skin skin;
	private Stage stage;
	private Table layoutGridContainer;
	private CommandsSystem commandsSystem;

	public TalosMain2(ILauncher launcher) {
		this.launcher = launcher;
	}

	@Override
	public void create () {
		super.create();

		AssetRepository.init();
		SharedResources.projectLoader = this::projectLoader;
		SharedResources.appManager = new AppManager();
		SharedResources.inputHandling = new InputHandling();
		SharedResources.globalDragAndDrop = new GlobalDragAndDrop();
		SharedResources.globalSaveStateSystem = new GlobalSaveStateSystem();
		SharedResources.configData = new ConfigData();
		TalosVFXUtils.talosAssetProvider = new TalosAssetProvider();
		SaveSystem saveSystem = new SaveSystem();

		TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("skin/uiskin.atlas"));
		skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
		skin.addRegions(atlas);

		VisUI.load(skin);
		SharedResources.skin = skin;

		TalosVFXUtils.init();

		stage = new Stage(new ScreenViewport(), new PolygonSpriteBatchMultiTextureMULTIBIND());

		SharedResources.stage = stage;
		SharedResources.ui = new UIController();

		layoutGridContainer = new Table();

		Table fullScreen = new Table();
		fullScreen.setFillParent(true);

		Table topBar = new Table();
		topBar.setBackground(SharedResources.skin.getDrawable("top-menu-bg"));
		fullScreen.add(topBar).growX().height(30);
		fullScreen.row();
		MainMenu menu = new MainMenu();
		menu.buildFrom(Gdx.files.internal("menuBar.xml"));
		SharedResources.mainMenu = menu;
		topBar.add(menu).grow().padLeft(4);

		fullScreen.add(layoutGridContainer).grow();

		stage.addActor(fullScreen);

		commandsSystem = new CommandsSystem();
		SharedResources.inputHandling.addPermanentInputProcessor(commandsSystem);
		SharedResources.inputHandling.addPermanentInputProcessor(stage);
		SharedResources.inputHandling.setGDXMultiPlexer();

		SharedResources.talosControl = new TalosControl();

		Notifications.quickFire(FinishInitializingEvent.class);

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

		projectData.loadLayout();
	}


	private void openProjectExplorer () {
		ProjectSplash projectSplash = new ProjectSplash();
		projectSplash.show(stage);
	}

	@Override
	public void render () {
		CursorUtil.checkAndReset();

		super.render();
		Gdx.gl.glClearColor(0.13f, 0.13f, 0.13f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		commandsSystem.act(Gdx.graphics.getDeltaTime());
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
		SocketServer.dispose();

		launcher.dispose();
	}
}
