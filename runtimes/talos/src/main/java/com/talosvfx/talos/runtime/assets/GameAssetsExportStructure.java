package com.talosvfx.talos.runtime.assets;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.runtime.scene.SceneData;

public class GameAssetsExportStructure {
	public Array<GameAssetExportStructure> gameAssets = new Array<>();

	public SceneData sceneData = new SceneData();

	private transient ObjectMap<String, GameAssetExportStructure> cache = new ObjectMap<>();

	private transient boolean cacheBuilt = false;

	public GameAssetExportStructure findAsset (String uuid) {
		if (!cacheBuilt) {
			buildCache();
		}
		return cache.get(uuid);
	}

	private void buildCache () {
		for (GameAssetExportStructure gameAsset : gameAssets) {
			cache.put(gameAsset.uuid, gameAsset);
		}
	}
}
