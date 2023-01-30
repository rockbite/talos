package com.talosvfx.talos.editor.project2;

import com.artemis.utils.reflect.ClassReflection;
import com.artemis.utils.reflect.ReflectionException;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.editor.addons.scene.apps.spriteeditor.SpriteEditorApp;
import com.talosvfx.talos.editor.addons.scene.apps.routines.RoutineEditorApp;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.runtime.assets.RawAsset;
import com.talosvfx.talos.editor.addons.scene.events.save.SaveRequest;
import com.talosvfx.talos.editor.layouts.LayoutApp;
import com.talosvfx.talos.editor.layouts.LayoutContent;
import com.talosvfx.talos.editor.layouts.LayoutGrid;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.Observer;
import com.talosvfx.talos.editor.notifications.events.FinishInitializingEvent;
import com.talosvfx.talos.editor.project2.apps.*;
import com.talosvfx.talos.editor.project2.apps.preferences.ContainerOfPrefs;
import com.talosvfx.talos.editor.project2.localprefs.TalosLocalPrefs;
import com.talosvfx.talos.editor.widgets.ui.menu.MainMenu;
import com.talosvfx.talos.runtime.assets.meta.EmptyMetadata;
import lombok.Getter;

import java.util.UUID;

public class AppManager extends InputAdapter implements Observer {

	private static final Object dummyObject = new Object();
	public static final GameAsset<Object> singletonAsset = new GameAsset<>("singleton", GameAssetType.DIRECTORY);
	public static final GameAsset<Object> dummyAsset = new GameAsset<>("dummy", GameAssetType.DIRECTORY);

	static {
		FileHandle singleton = Gdx.files.local("singleton");
		RawAsset value = new RawAsset(singleton);
		value.metaData = new EmptyMetadata();
		value.metaData.uuid = new UUID(-1, -1);
		singletonAsset.dependentRawAssets.add(value);

		FileHandle dummy = Gdx.files.local("dummy");
		RawAsset dummyValue = new RawAsset(dummy);
		dummyValue.metaData = new EmptyMetadata();
		dummyValue.metaData.uuid = new UUID(-1, -2);
		dummyAsset.dependentRawAssets.add(dummyValue);
	}
	private static final String APP_LIST_MENU_PATH = "window/apps/list";
	private static final String PANEL_LIST_MENU_PATH = "window/panels/panel_list";

	static {
		singletonAsset.setResourcePayload(dummyObject);
	}

	private MainMenu.IMenuProvider menuOpenAppListProvider;
	private MainMenu.IMenuProvider menuAppListProvider;

	public BaseApp getAppForLayoutApp (LayoutApp app) {

		for (ObjectMap.Entry<GameAsset<?>, Array<? extends BaseApp<?>>> gameAssetArrayEntry : baseAppsOpenForGameAsset) {
			Array<? extends BaseApp<?>> apps = gameAssetArrayEntry.value;
			for (int i = 0; i < apps.size; i++) {
				BaseApp<?> baseApp = apps.get(i);
				if (baseApp.gridAppReference == app) {
					//Its this app, lets return it
					return baseApp;
				}
			}
		}

		return null;
	}

	public <T, U extends BaseApp<T>> U createAndRegisterAppExternal (String appID, String baseAppClazz, GameAssetType gameAssetType, String gameAssetIdentifier, UUID gameAssetUniqueIdentifier) {

		Class<U> appForSimpleName = (Class<U>)appRegistry.getAppForSimpleName(baseAppClazz);

		if (appForSimpleName == null) {
			throw new GdxRuntimeException("No app found for clazz " + baseAppClazz + " register it in AppManager");
		}

		GameAsset<T> gameAsset = null;
		if (gameAssetIdentifier.equals("singleton") && gameAssetType == GameAssetType.DIRECTORY) {
			gameAsset = (GameAsset<T>)singletonAsset;
		} else if (gameAssetIdentifier.equals("dummy")) {
			//We find the first asset of the type and use that
			gameAsset = AssetRepository.getInstance().findFirstOfType(gameAssetType);
			if (gameAsset == null) {
				//Create a dummy of that type
				gameAsset = AssetRepository.getInstance().createDummyAsset(gameAssetType);
			}
		} else {
			if (gameAssetUniqueIdentifier != null) {
				gameAsset = AssetRepository.getInstance().getAssetForUniqueIdentifier(gameAssetUniqueIdentifier, gameAssetType);
			}
			if (gameAsset == null) {
				gameAsset = AssetRepository.getInstance().getAssetForIdentifier(gameAssetIdentifier, gameAssetType);
			}
		}

		U baseAppForGameAsset = createBaseAppForGameAsset(gameAsset, appForSimpleName);
		baseAppForGameAsset.setAppID(appID);

		if (!baseAppsOpenForGameAsset.containsKey(gameAsset)) {
			baseAppsOpenForGameAsset.put(gameAsset, new Array<>());
		}
		Array<BaseApp<T>> baseApps = (Array<BaseApp<T>>)baseAppsOpenForGameAsset.get(gameAsset);
		baseApps.add(baseAppForGameAsset);

		SharedResources.mainMenu.askToInject(menuOpenAppListProvider, PANEL_LIST_MENU_PATH);

		return baseAppForGameAsset;
	}

	public void onAppRemoved (LayoutApp layoutApp) {
		for (ObjectMap.Entry<GameAsset<?>, Array<? extends BaseApp<?>>> gameAssetArrayEntry : baseAppsOpenForGameAsset) {
			Array<? extends BaseApp<?>> apps = gameAssetArrayEntry.value;
			for (int i = apps.size - 1; i >= 0; i--) {
				BaseApp<?> baseApp = apps.get(i);
				if (baseApp.getGridAppReference() == layoutApp) {
					BaseApp<?> appToRemove = apps.removeIndex(i);
					appToRemove.onRemove();
				}
			}
		}

		SharedResources.mainMenu.askToInject(menuOpenAppListProvider, PANEL_LIST_MENU_PATH);
	}

	public Array<BaseApp> getAppInstances() {
		Array<BaseApp> result = new Array<>();
		for (ObjectMap.Entry<GameAsset<?>, Array<? extends BaseApp<?>>> gameAssetArrayEntry : baseAppsOpenForGameAsset) {
			Array<? extends BaseApp<?>> apps = gameAssetArrayEntry.value;
			for (int i = apps.size - 1; i >= 0; i--) {
				BaseApp<?> baseApp = apps.get(i);
				result.add(baseApp);
			}
		}

		return result;
	}

	public <T extends BaseApp> T getSingletonAppInstance(Class<T> appClass) {
		// todo: write this
		Array<BaseApp> appInstances = getAppInstances();
		for(BaseApp app: appInstances) {
			if(!app.singleton) {
				continue;
			}
			if(app.getClass().equals(appClass)) {
				return (T) app;
			}
		}
		return null;
	}

	public <T extends BaseApp> T  getAppForAsset(Class<T> appClass, GameAsset<?> gameAsset) {
		Array<BaseApp> appInstances = getAppInstances();
		for(BaseApp app: appInstances) {
			if(appClass.isAssignableFrom(app.getClass()) && app.gameAsset != null && app.gameAsset == gameAsset) {
				return (T) app;
			}
		}

		return null;
	}

	//	app manager that interacts, some are singletons, some are per instances, all are tied to some kind of object

	//Grid layout needs to be able to be setup with layout specific in mind

	public abstract static class BaseApp<T> {
		protected boolean singleton;

		@Getter
		protected LayoutApp gridAppReference;

		@Getter
		protected GameAsset<T> gameAsset;

		public void updateForGameAsset (GameAsset<T> gameAsset) {
			this.gameAsset = gameAsset;

		}

		public abstract String getAppName ();

		public GameAssetType getGameAssetType () {
			return gameAsset.type;
		}

		public String getAssetIdentifier () {
			return gameAsset.nameIdentifier;
		}

		public void setAppID (String appID) {
			getGridAppReference().setUniqueIdentifier(appID);
		}

		public abstract void onRemove ();

		public boolean hasChangesToSave () {
			return SharedResources.globalSaveStateSystem.isItemChangedAndUnsaved(gameAsset);
		}
	}

	private static class AppRegistry {

		private ObjectMap<String, Class<? extends BaseApp<?>>> simpleNameMap = new ObjectMap<>();
		private ObjectMap<GameAssetType, Array<Class<? extends BaseApp<?>>>> gameAssetTypeToAppsMap = new ObjectMap<>();

		private <T, U extends BaseApp<T>> void registerAppsForAssetType (GameAssetType gameAssetType, Class<? extends U>... classes) {
			for (Class<? extends U> aClass : classes) {
				if (!gameAssetTypeToAppsMap.containsKey(gameAssetType)) {
					gameAssetTypeToAppsMap.put(gameAssetType, new Array<>());
				}

				//Type unsafe part, resitrcted
				Array classesArray = gameAssetTypeToAppsMap.get(gameAssetType);
				classesArray.add(aClass);
				simpleNameMap.put(aClass.getSimpleName(), aClass);
			}
		}

		public void addExternalClass (Class<? extends BaseApp<?>> clazz) {
			simpleNameMap.put(clazz.getSimpleName(), clazz);
		}

		public Class<? extends BaseApp<?>> getAppForSimpleName (String baseAppClazz) {
			if (baseAppClazz == null) {
				System.out.println();
			}
			return simpleNameMap.get(baseAppClazz);
		}

		public <T, U extends BaseApp<T>> Array<Class<U>> getAppsForGameAssetType (GameAsset<T> gameAsset) {
			GameAssetType gameAssetType = gameAsset.type;
			if (gameAssetTypeToAppsMap.containsKey(gameAssetType)) {

				//Type unsafe part
				Array classes = gameAssetTypeToAppsMap.get(gameAssetType);
				return classes;
			} else {
				return new Array<>();
			}
		}

		public boolean hasAssetType (GameAssetType type) {
			return gameAssetTypeToAppsMap.containsKey(type);
		}


	}

	private ObjectMap<GameAsset<?>, Array<? extends BaseApp<?>>> baseAppsOpenForGameAsset = new ObjectMap<>();

	private AppRegistry appRegistry = new AppRegistry();

	public AppManager () {
		appRegistry.addExternalClass(ProjectExplorerApp.class);

		appRegistry.registerAppsForAssetType(GameAssetType.PREFAB, SceneEditorApp.class, SceneHierarchyApp.class );
		appRegistry.registerAppsForAssetType(GameAssetType.PREFAB, PropertiesPanelApp.class);
		appRegistry.registerAppsForAssetType(GameAssetType.SCENE, SceneEditorApp.class, SceneHierarchyApp.class, ScenePreviewApp.class);
		appRegistry.registerAppsForAssetType(GameAssetType.SCENE, PropertiesPanelApp.class);

		appRegistry.registerAppsForAssetType(GameAssetType.VFX, ParticleNodeEditorApp.class, ParticlePreviewApp.class, EmitterTimelineApp.class);
		appRegistry.registerAppsForAssetType(GameAssetType.SPRITE, SpriteEditorApp.class);
		appRegistry.registerAppsForAssetType(GameAssetType.ROUTINE, RoutineEditorApp.class);

		Notifications.registerObserver(this);
	}

	public <T> boolean canOpenInTalos (GameAsset<T> gameAsset) {
		return appRegistry.hasAssetType(gameAsset.type);
	}

	public <T, U extends BaseApp<T>> U openAppIfNotOpened (GameAsset<T> asset, Class<U> app) {
		Array<? extends BaseApp<?>> baseApps = baseAppsOpenForGameAsset.get(asset);
		for (BaseApp<?> baseApp : baseApps) {
			if(baseApp.getClass().equals(app)) {
				if(!baseApp.gridAppReference.isTabActive()) {
					baseApp.gridAppReference.getLayoutContent().swapToApp(baseApp.getGridAppReference());
				}
				return (U) baseApp;
			}
		}

		return openApp(asset, app);
	}


	public <T, U extends BaseApp<T>> U openApp (GameAsset<T> asset, Class<U> app) {
		U baseAppForGameAsset = createBaseAppForGameAsset(asset, app);
		createAppAndPlaceInGrid(asset, SharedResources.currentProject.getLayoutGrid(), baseAppForGameAsset);

		SharedResources.mainMenu.askToInject(menuOpenAppListProvider, PANEL_LIST_MENU_PATH);

		return baseAppForGameAsset;
	}

	private static class LayoutGridTargetConfig {
		LayoutContent target; //null for root
		LayoutGrid.LayoutDirection direction;
	}

	public <T, U extends BaseApp<T>> void openNewAsset (GameAsset<T> gameAsset) {

		//open vfx.
		//Open preview, timeline, node editor

		//Open scene, hierarchy, properties, sceneeditor workspace, assetexplorer

		//Singleton, replace scene

		//For each window to open, lets

		//find apps that need creating
		//find apps that need exchanging (singletons that need to be injected)

		//For each app to be created, find the target config for grid layout, (tab, direction, layout etc)\
		//For each app to be exchanged, we just update it

		LayoutGrid layoutGrid = SharedResources.currentProject.getLayoutGrid();

		Array<U> appsToUpdate = new Array<>(getAppsToUpdate(gameAsset));
		Array<U> appsToCreate = getAppsToCreateAndOpen(gameAsset, appsToUpdate);

		for (U baseApp : appsToCreate) {
			createAppAndPlaceInGrid(gameAsset, layoutGrid, baseApp);
		}

		if(!appsToCreate.isEmpty()) {
			SharedResources.mainMenu.askToInject(menuOpenAppListProvider, PANEL_LIST_MENU_PATH);
		}

		for (BaseApp<T> baseApp : appsToUpdate) {
			baseApp.updateForGameAsset(gameAsset);
		}

	}

	public void removeAll () {
		Array<BaseApp> appsToRemove = new Array<>();
		for (ObjectMap.Entry<GameAsset<?>, Array<? extends BaseApp<?>>> gameAssetArrayEntry : baseAppsOpenForGameAsset) {
			Array<? extends BaseApp<?>> apps = gameAssetArrayEntry.value;
			appsToRemove.addAll(apps);
		}
		for (BaseApp baseApp : appsToRemove) {
			baseApp.getGridAppReference().getDestroyCallback().onDestroyRequest();
		}
		baseAppsOpenForGameAsset.clear();
	}

	private <T, U extends BaseApp<T>> void createAppAndPlaceInGrid (GameAsset<T> gameAsset, LayoutGrid layoutGrid, U baseApp) {
		LayoutGridTargetConfig placementConfig = getBestPlacementForApp(baseApp, layoutGrid);

		if (placementConfig.target == null) {
			layoutGrid.addContent(new LayoutContent(SharedResources.skin, layoutGrid, baseApp.gridAppReference));
			//Its root, so we should add it to root via addContent
		} else {
			//Add it externally
			if (placementConfig.direction == LayoutGrid.LayoutDirection.TAB) {
				placementConfig.target.addContent(baseApp.gridAppReference);
			} else {
				layoutGrid.placeContentRelative(placementConfig.target, placementConfig.direction, baseApp.gridAppReference);

			}
		}
		if (!baseAppsOpenForGameAsset.containsKey(gameAsset)) {
			baseAppsOpenForGameAsset.put(gameAsset, new Array<>());
		}
		Array<BaseApp<T>> baseApps = (Array<BaseApp<T>>)baseAppsOpenForGameAsset.get(gameAsset);
		baseApps.add(baseApp);
	}

	private LayoutGridTargetConfig getBestPlacementForApp (BaseApp app, LayoutGrid layoutGrid) {
		LayoutGridTargetConfig layoutGridTargetConfig = new LayoutGridTargetConfig();


		if (checkCanTab(layoutGridTargetConfig, app)) {
			return layoutGridTargetConfig;
		}

		//todo be smart

		//Default
		layoutGridTargetConfig.target = null;
		layoutGridTargetConfig.direction = LayoutGrid.LayoutDirection.LEFT;

		return layoutGridTargetConfig;
	}

	private boolean checkCanTab (LayoutGridTargetConfig layoutGridTargetConfig, BaseApp app) {

		GameAssetType gameAssetType = app.getGameAssetType();


		//Check exact types first
		for (Array<? extends BaseApp<?>> baseAppArray : baseAppsOpenForGameAsset.values()) {
			for (int i = 0; i < baseAppArray.size; i++) {
				BaseApp<?> baseApp = baseAppArray.get(i);
				if (baseApp.getClass().equals(app.getClass())) {
					layoutGridTargetConfig.target = baseApp.gridAppReference.getLayoutContent();
					layoutGridTargetConfig.direction = LayoutGrid.LayoutDirection.TAB;
					return true;
				}
			}
		}

		for (Array<? extends BaseApp<?>> baseAppArray : baseAppsOpenForGameAsset.values()) {
			for (int i = 0; i < baseAppArray.size; i++) {
				BaseApp<?> baseApp = baseAppArray.get(i);
				if (canTabTypes(baseApp.getGameAssetType(), gameAssetType)) {
					layoutGridTargetConfig.target = baseApp.gridAppReference.getLayoutContent();
					layoutGridTargetConfig.direction = LayoutGrid.LayoutDirection.TAB;
					return true;
				}
			}
		}

		return false;
	}

	private boolean canTabTypes (GameAssetType gameAssetType, GameAssetType appToCreateType) {
		if (gameAssetType == appToCreateType) return true;

		if (gameAssetType == GameAssetType.ROUTINE && appToCreateType == GameAssetType.SCENE) return true;
		if (gameAssetType == GameAssetType.ROUTINE && appToCreateType == GameAssetType.PREFAB) return true;
		if (gameAssetType == GameAssetType.SCENE && appToCreateType == GameAssetType.ROUTINE) return true;
		if (gameAssetType == GameAssetType.SCENE && appToCreateType == GameAssetType.PREFAB) return true;
		if (gameAssetType == GameAssetType.PREFAB && appToCreateType == GameAssetType.ROUTINE) return true;
		if (gameAssetType == GameAssetType.PREFAB && appToCreateType == GameAssetType.SCENE) return true;

		return false;
	}

	private <T, U extends BaseApp<T>> Array<U> getAppsToUpdate (GameAsset<T> gameAsset) {
		if (baseAppsOpenForGameAsset.containsKey(gameAsset)) {
			//Unsafe type
			Array<U> baseApps = (Array<U>)baseAppsOpenForGameAsset.get(gameAsset);
			return baseApps;
		}
		return new Array<>();
	}

	private <T, U extends BaseApp<T>> U createBaseAppForGameAsset (GameAsset<T> gameAsset, Class<U> aClass) {
		U baseApp = null;
		try {
			baseApp = ClassReflection.newInstance(aClass);
		} catch (ReflectionException e) {
			throw new RuntimeException(e);
		}
		baseApp.updateForGameAsset(gameAsset);
		baseApp.getGridAppReference().updateTabName(baseApp.getAppName());
		return baseApp;
	}

	private <T, U extends BaseApp<T>> Array<U> getAppsToCreateAndOpen (GameAsset<T> gameAsset, Array<U> openApps) {

		Array<Class<U>> appsForGameAssetType = appRegistry.getAppsForGameAssetType(gameAsset);

		Array<U> baseAppsToCreate = new Array<>();

		for (Class<U> aClass : appsForGameAssetType) {
			//Check if we have one open in openApps
			boolean singleton = false;

			if (aClass.isAnnotationPresent(SingletonApp.class)) {
				singleton = true;
			}

			//todo do something with singleton

			boolean shouldSkip = false;
			for (int i = 0; i < openApps.size; i++) {
				U baseApp = openApps.get(i);
				if (baseApp.getClass() == aClass) {
					shouldSkip = true;//Its already open, so we should ignore it as its in the update list
					break;
				}
			}

			if (!shouldSkip) {
				//We should create the base app
				U baseAppForGameAsset = createBaseAppForGameAsset(gameAsset, aClass);
				baseAppsToCreate.add(baseAppForGameAsset);
			}

		}

		return baseAppsToCreate;
	}

	@EventHandler
	public void onFinishInitializing(FinishInitializingEvent event) {
		menuOpenAppListProvider = new MainMenu.IMenuProvider() {
			@Override
			public void inject(String path, MainMenu menu) {
				Array<BaseApp> appInstances = getAppInstances();

				for(BaseApp app: appInstances) {
					menu.addItem(path, app.getAppName(), app.getAppName(), null, app);
				}
			}
		};

		menuAppListProvider = new MainMenu.IMenuProvider() {
			@Override
			public void inject(String path, MainMenu menu) {
				for(ObjectMap.Entry<String, Class<? extends BaseApp<?>>> entry: appRegistry.simpleNameMap) {
					String name = entry.key;
					Class<? extends BaseApp<?>> clazz = entry.value;
					menu.addItem(path, name, name, null, clazz);
				}
			}
		};

		SharedResources.mainMenu.registerMenuProvider(menuOpenAppListProvider, PANEL_LIST_MENU_PATH);
		SharedResources.mainMenu.registerMenuProvider(menuAppListProvider, APP_LIST_MENU_PATH);
	}

	public void closeAllFloatingWindows() {
		// todo: close all floating windows
	}

	@EventHandler
	public void onSave (SaveRequest event) {
		// set preferences for all assets
		Array<BaseApp> appInstances = getAppInstances();

		for(BaseApp app: appInstances) {
			if (ContainerOfPrefs.class.isAssignableFrom(app.getClass())) {
				ContainerOfPrefs containerOfPrefs = (ContainerOfPrefs) app;
				TalosLocalPrefs.setAppPrefs(app.gameAsset, containerOfPrefs);
			}
		}

		// save preferences
		TalosLocalPrefs.savePrefs();

		//Save the selected app if it needs it

		for (BaseApp appInstance : appInstances) {
			boolean isFocused = appInstance.getGridAppReference().isTabFocused();

			if (isFocused) {
				GameAsset<?> gameAsset = appInstance.gameAsset;
				if (gameAsset.isDummy() || gameAsset == AppManager.singletonAsset) continue;

				boolean itemChangedAndUnsaved = SharedResources.globalSaveStateSystem.isItemChangedAndUnsaved(gameAsset);
				if (itemChangedAndUnsaved) {
					AssetRepository.getInstance().saveGameAssetResourceJsonToFile(gameAsset);
					return;
				}

			}

		}
	}

	private Vector2 vector2 = new Vector2();
	private Array<LayoutContent> out = new Array<>();
	@Override
	public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		vector2 = new Vector2(screenX, screenY);

		if (SharedResources.currentProject != null) {
			LayoutGrid layoutGrid = SharedResources.currentProject.getLayoutGrid();
			out.clear();
			layoutGrid.getAllLayoutContentsFlat(out);

			for (LayoutContent layoutContent : out) {
				layoutContent.screenToLocalCoordinates(vector2.set(screenX, screenY));
				if (layoutContent.hit(vector2.x, vector2.y, false) != null) {
					layoutGrid.setLayoutActive(layoutContent);
				}
			}
		}

		return false; //Never do anything with touches, its just passive detection
	}
}
