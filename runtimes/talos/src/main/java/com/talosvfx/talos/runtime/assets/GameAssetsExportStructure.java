package com.talosvfx.talos.runtime.assets;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.runtime.scene.SceneData;
import com.talosvfx.talos.runtime.scene.SceneLayer;

public class GameAssetsExportStructure {

	public String talosIdentifier;
	public Array<GameAssetExportStructure> gameAssets = new Array<>();

	public SceneData sceneData = new SceneData();

	private transient ObjectMap<String, GameAssetExportStructure> cache = new ObjectMap<>();
	private transient ObjectMap<GameAssetType, ObjectMap<String, GameAssetExportStructure>> identifierCache = new ObjectMap<>();

	private transient boolean cacheBuilt = false;

	public long ageOfYoungestAsset; // max timestamp of exported sprite and atlas assets

	public GameAssetExportStructure findAsset (String uuid) {
		if (!cacheBuilt) {
			buildCache();
		}
		return cache.get(uuid);
	}

	public GameAssetExportStructure findAsset (String identifier, GameAssetType type) {
		if (!cacheBuilt){
			buildCache();
		}
		return identifierCache.get(type).get(identifier);
	}

	public void buildLayerIndices() {
		Array<SceneLayer> renderLayers = sceneData.getRenderLayers();
		for (int i = 0; i < renderLayers.size; i++) {
			SceneLayer sceneLayer = renderLayers.get(i);
			sceneLayer.setIndex(i);
		}
	}

	private void buildCache () {
		for (GameAssetExportStructure gameAsset : gameAssets) {
			cache.put(gameAsset.uuid, gameAsset);

			if (!identifierCache.containsKey(gameAsset.type)) {
				identifierCache.put(gameAsset.type, new ObjectMap<>());
			}

			identifierCache.get(gameAsset.type).put(gameAsset.identifier, gameAsset);
		}

		cacheBuilt = true;
	}
}
