package com.talosvfx.talos.editor.project2;

import com.artemis.utils.reflect.ClassReflection;
import com.artemis.utils.reflect.ReflectionException;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.GameAssetType;
import com.talosvfx.talos.editor.layouts.LayoutApp;
import com.talosvfx.talos.editor.layouts.LayoutContent;
import com.talosvfx.talos.editor.layouts.LayoutGrid;
import com.talosvfx.talos.editor.project2.apps.ParticleNodeEditorApp;
import com.talosvfx.talos.editor.project2.apps.ParticlePreviewApp;

public class AppManager {

	//	app manager that interacts, some are singletons, some are per instances, all are tied to some kind of object

	//Grid layout needs to be able to be setup with layout specific in mind

	public abstract static class BaseApp<T> {
		protected boolean singleton;
		protected LayoutApp gridAppReference;

		protected GameAsset<T> gameAsset;
		public void updateForGameAsset (GameAsset<T> gameAsset) {
			this.gameAsset = gameAsset;
		}

		public abstract String getAppName ();

	}

	private static class AppRegistry {

		private ObjectMap<GameAssetType, Array<Class<? extends BaseApp>>> gameAssetTypeToAppsMap = new ObjectMap<>();

		private void registerAppsForAssetType (GameAssetType gameAssetType, Class<? extends BaseApp>... classes) {
			for (Class<? extends BaseApp> aClass : classes) {
				if (!gameAssetTypeToAppsMap.containsKey(gameAssetType)) {
					gameAssetTypeToAppsMap.put(gameAssetType, new Array<>());
				}
				gameAssetTypeToAppsMap.get(gameAssetType).add(aClass);
			}
		}

		public Array<Class<? extends BaseApp>> getAppsForGameAssetType (GameAssetType gameAssetType) {
			if (gameAssetTypeToAppsMap.containsKey(gameAssetType)) {
				return gameAssetTypeToAppsMap.get(gameAssetType);
			} else {
				return new Array<>();
			}
		}

		public boolean hasAssetType (GameAssetType type) {
			return gameAssetTypeToAppsMap.containsKey(type);
		}
	}

	private ObjectMap<GameAsset<?>, Array<? extends BaseApp>> baseAppsOpenForGameAsset = new ObjectMap<>();

	private AppRegistry appRegistry = new AppRegistry();

	public AppManager () {
		appRegistry.registerAppsForAssetType(GameAssetType.VFX, ParticleNodeEditorApp.class, ParticlePreviewApp.class);
	}

	public <T> boolean canOpenInTalos (GameAsset<T> gameAsset) {
		return appRegistry.hasAssetType(gameAsset.type);
	}

	private static class LayoutGridTargetConfig {
		LayoutContent target; //null for root
		LayoutGrid.LayoutDirection direction;
	}


	public <T> void openNewAsset (GameAsset<T> gameAsset) {

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

		Array<? extends BaseApp> appsToUpdate = getAppsToUpdate(gameAsset);
		Array<? extends BaseApp> appsToCreate = getAppsToCreateAndOpen(gameAsset, appsToUpdate);

		for (BaseApp baseApp : appsToCreate) {
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
		}

		for (BaseApp baseApp : appsToUpdate) {
			baseApp.updateForGameAsset(gameAsset);
		}

	}

	private LayoutGridTargetConfig getBestPlacementForApp (BaseApp app, LayoutGrid layoutGrid) {
		LayoutGridTargetConfig layoutGridTargetConfig = new LayoutGridTargetConfig();

		//todo be smart
		layoutGridTargetConfig.target = null;
		layoutGridTargetConfig.direction = LayoutGrid.LayoutDirection.LEFT;

		return layoutGridTargetConfig;
	}

	private Array<? extends BaseApp> getAppsToUpdate (GameAsset<?> gameAsset) {
		if (baseAppsOpenForGameAsset.containsKey(gameAsset)) {
			Array<? extends BaseApp> baseApps = baseAppsOpenForGameAsset.get(gameAsset);
			return baseApps;
		}
		return new Array<>();
	}

	private <T> BaseApp createBaseAppForGameAsset (GameAsset<T> gameAsset, Class<? extends BaseApp> aClass) {
		BaseApp baseApp = null;
		try {
			baseApp = ClassReflection.newInstance(aClass);
		} catch (ReflectionException e) {
			throw new RuntimeException(e);
		}
		baseApp.updateForGameAsset(gameAsset);
		return baseApp;
	}
	private <T> Array<BaseApp> getAppsToCreateAndOpen (GameAsset<T> gameAsset, Array<? extends BaseApp> openApps) {

		Array<Class<? extends BaseApp>> appsForGameAssetType = appRegistry.getAppsForGameAssetType(gameAsset.type);

		Array<BaseApp> baseAppsToCreate = new Array<>();

		for (Class<? extends BaseApp> aClass : appsForGameAssetType) {
			//Check if we have one open in openApps

			boolean shouldSkip = false;
			for (int i = 0; i < openApps.size; i++) {
				BaseApp baseApp = openApps.get(i);
				if (baseApp.getClass() == aClass) {
					shouldSkip = true;//Its already open, so we should ignore it as its in the update list
					break;
				}
			}

			if (!shouldSkip) {
				//We should create the base app
				baseAppsToCreate.add(createBaseAppForGameAsset(gameAsset, aClass));
			}

		}

		return baseAppsToCreate;
	}



}
