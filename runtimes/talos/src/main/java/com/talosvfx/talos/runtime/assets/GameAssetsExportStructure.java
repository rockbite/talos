package com.talosvfx.talos.runtime.assets;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

public class GameAssetsExportStructure {
	public Array<GameAssetExportStructure> gameAssets = new Array<>();

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
