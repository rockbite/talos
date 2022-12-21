package com.talosvfx.talos.editor.project2;

import com.artemis.utils.reflect.ClassReflection;
import com.artemis.utils.reflect.ReflectionException;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.editor.addons.scene.apps.spriteeditor.SpriteEditorApp;
import com.talosvfx.talos.editor.addons.scene.apps.routines.RoutineEditorApp;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.GameAssetType;
import com.talosvfx.talos.editor.layouts.LayoutApp;
import com.talosvfx.talos.editor.layouts.LayoutContent;
import com.talosvfx.talos.editor.layouts.LayoutGrid;
import com.talosvfx.talos.editor.project2.apps.ParticleNodeEditorApp;
import com.talosvfx.talos.editor.project2.apps.ParticlePreviewApp;
import com.talosvfx.talos.editor.project2.apps.ProjectExplorerApp;
import com.talosvfx.talos.editor.project2.apps.PropertiesPanelApp;
import com.talosvfx.talos.editor.project2.apps.SceneEditorApp;
import com.talosvfx.talos.editor.project2.apps.SceneHierarchyApp;
import com.talosvfx.talos.editor.project2.apps.SingletonApp;
import lombok.Getter;

public class AppManager {

	private static final Object dummyObject = new Object();
	public static final GameAsset<Object> singletonAsset = new GameAsset<>("singleton", GameAssetType.DIRECTORY);

	static {
		singletonAsset.setResourcePayload(dummyObject);
	}

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

	public <T, U extends BaseApp<T>> U createAndRegisterAppExternal (String appID, String baseAppClazz, GameAssetType gameAssetType, String gameAssetIdentifier) {

		Class<U> appForSimpleName = (Class<U>)appRegistry.getAppForSimpleName(baseAppClazz);

		if (appForSimpleName == null) {
			throw new GdxRuntimeException("No app found for clazz " + baseAppClazz + " register it in AppManager");
		}

		GameAsset<T> gameAsset;
		if (gameAssetIdentifier.equals("singleton") && gameAssetType == GameAssetType.DIRECTORY) {
			gameAsset = (GameAsset<T>)singletonAsset;
		} else {
			gameAsset = AssetRepository.getInstance().getAssetForIdentifier(gameAssetIdentifier, gameAssetType);
		}

		U baseAppForGameAsset = createBaseAppForGameAsset(gameAsset, appForSimpleName);
		baseAppForGameAsset.setAppID(appID);

		if (!baseAppsOpenForGameAsset.containsKey(gameAsset)) {
			baseAppsOpenForGameAsset.put(gameAsset, new Array<>());
		}
		Array<BaseApp<T>> baseApps = (Array<BaseApp<T>>)baseAppsOpenForGameAsset.get(gameAsset);
		baseApps.add(baseAppForGameAsset);

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

		appRegistry.registerAppsForAssetType(GameAssetType.SCENE, SceneEditorApp.class, SceneHierarchyApp.class);
		appRegistry.registerAppsForAssetType(GameAssetType.SCENE, PropertiesPanelApp.class);

		appRegistry.registerAppsForAssetType(GameAssetType.VFX, ParticleNodeEditorApp.class, ParticlePreviewApp.class);
		appRegistry.registerAppsForAssetType(GameAssetType.SPRITE, SpriteEditorApp.class);
		appRegistry.registerAppsForAssetType(GameAssetType.ROUTINE, RoutineEditorApp.class);
	}

	public <T> boolean canOpenInTalos (GameAsset<T> gameAsset) {
		return appRegistry.hasAssetType(gameAsset.type);
	}

	public <T, U extends BaseApp<T>> void openApp (GameAsset<T> asset, Class<U> app) {
		U baseAppForGameAsset = createBaseAppForGameAsset(asset, app);
		createAppAndPlaceInGrid(asset, SharedResources.currentProject.getLayoutGrid(), baseAppForGameAsset);
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

		Array<U> appsToUpdate = getAppsToUpdate(gameAsset);
		Array<U> appsToCreate = getAppsToCreateAndOpen(gameAsset, appsToUpdate);

		for (U baseApp : appsToCreate) {
			createAppAndPlaceInGrid(gameAsset, layoutGrid, baseApp);
		}

		for (BaseApp<T> baseApp : appsToUpdate) {
			baseApp.updateForGameAsset(gameAsset);
		}

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

		//todo be smart
		layoutGridTargetConfig.target = null;
		layoutGridTargetConfig.direction = LayoutGrid.LayoutDirection.LEFT;

		return layoutGridTargetConfig;
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

}
