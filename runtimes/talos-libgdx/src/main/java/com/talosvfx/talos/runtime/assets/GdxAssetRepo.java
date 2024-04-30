package com.talosvfx.talos.runtime.assets;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.talosvfx.talos.runtime.RuntimeContext;

public class GdxAssetRepo extends RuntimeAssetRepository {


	public void loadBundleFromFile (FileHandle repoFile) {
		GameAssetsExportStructure gameAssetsExportStructure = new Json().fromJson(GameAssetsExportStructure.class, repoFile);

		RuntimeContext.TalosContext talosContext = new RuntimeContext.TalosContext(gameAssetsExportStructure.talosIdentifier);
		talosContext.setBaseAssetRepository(this);

		RuntimeContext.getInstance().registerContext(talosContext.getIdentifier(), talosContext);


		gameAssetsExportStructure.buildLayerIndices();
		loadBundle(gameAssetsExportStructure, repoFile.parent());

	}
}
