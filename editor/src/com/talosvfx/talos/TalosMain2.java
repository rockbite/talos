package com.talosvfx.talos;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.rockbite.bongo.engine.render.PolygonSpriteBatchMultiTextureMULTIBIND;
import com.rockbite.bongo.engine.systems.RenderPassSystem;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.commands.CommandsSystem;
import com.talosvfx.talos.editor.notifications.events.FinishInitializingEvent;
import com.talosvfx.talos.editor.notifications.events.ProjectLoadedEvent;
import com.talosvfx.talos.editor.project2.*;
import com.talosvfx.talos.editor.project2.debug.DebugUtils;
import com.talosvfx.talos.editor.project2.localprefs.TalosLocalPrefs;
import com.talosvfx.talos.editor.project2.input.InputHandling;
import com.talosvfx.talos.editor.project2.savestate.GlobalSaveStateSystem;
import com.talosvfx.talos.editor.project2.savestate.SaveSystem;
import com.talosvfx.talos.editor.socket.SocketServer;
import com.talosvfx.talos.editor.utils.CursorUtil;
import com.talosvfx.talos.editor.utils.Toasts;
import com.talosvfx.talos.editor.widgets.ui.menu.MainMenu;
import com.talosvfx.talos.runtime.RuntimeContext;
import com.talosvfx.talos.runtime.assets.meta.DefaultConstants;
import com.talosvfx.talos.runtime.routine.RoutineEventInterface;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyWrapper;
import com.talosvfx.talos.runtime.utils.ConfigData;
import com.talosvfx.talos.runtime.utils.Supplier;
import lombok.Getter;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class TalosMain2 extends ApplicationAdapter {

	private static final Logger logger = LoggerFactory.getLogger(TalosMain2.class);
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
		commandsSystem = new CommandsSystem();

		AssetRepository.init();
		SharedResources.debug = new DebugUtils();
		SharedResources.projectLoader = new ProjectLoaderImpl();
		SharedResources.appManager = new AppManager();
		SharedResources.inputHandling = new InputHandling();
		SharedResources.globalDragAndDrop = new GlobalDragAndDrop();
		SharedResources.globalSaveStateSystem = new GlobalSaveStateSystem();
		SharedResources.commandsSystem = this.commandsSystem;

		DefaultConstants.defaultPixelPerUnitProvider = new Supplier<Float>() {
			@Override
			public Float get () {
				return Float.parseFloat(SharedResources.currentProject.getDefaultPixelPerMeter());
			}
		};

		RuntimeContext instance = RuntimeContext.getInstance();
		instance.AssetRepository = AssetRepository.getInstance();

		SaveSystem saveSystem = new SaveSystem();

		TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("skin/uiskin.atlas"));
		skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
		skin.addRegions(atlas);

		VisUI.load(skin);
		SharedResources.skin = skin;

		TalosVFXUtils.init();

		stage = new SharedStage(new ScreenViewport(), new PolygonSpriteBatch(3000));

		SharedResources.stage = stage;
		SharedResources.ui = new UIController();

		layoutGridContainer = new Table();
		((ProjectLoaderImpl) SharedResources.projectLoader).setLayoutGridContainer(layoutGridContainer);

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

		SharedResources.inputHandling.addPermanentInputProcessor(stage);
		SharedResources.inputHandling.setGDXMultiPlexer();

		SharedResources.talosControl = new TalosControl();

		Notifications.quickFire(FinishInitializingEvent.class);

		openProjectExplorer();

		RuntimeContext.getInstance().routineEventInterface = new RoutineEventInterface() {
            @Override
            public void onEventFromRoutines(String eventName, Array<PropertyWrapper<?>> properties) {
                Toasts.getInstance().showInfoToast(eventName + " event fired!");
            }
        };
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
