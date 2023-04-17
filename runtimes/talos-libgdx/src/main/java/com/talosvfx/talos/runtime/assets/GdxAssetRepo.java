package com.talosvfx.talos.runtime.assets;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;

public class GdxAssetRepo extends RuntimeAssetRepository {


	public void loadBundleFromFile (FileHandle repoFile) {
		GameAssetsExportStructure gameAssetsExportStructure = new Json().fromJson(GameAssetsExportStructure.class, repoFile);
		gameAssetsExportStructure.buildLayerIndices();
		loadBundle(gameAssetsExportStructure, repoFile.parent());

	}
}
