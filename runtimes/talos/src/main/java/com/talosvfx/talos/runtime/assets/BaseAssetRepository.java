package com.talosvfx.talos.runtime.assets;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.utils.GdxRuntimeException;

public abstract class BaseAssetRepository {
	public static class AssetRepositoryCatalogueExportOptions {
		boolean forceExportAll = true;

	}
	public abstract void reloadGameAssetForRawFile (RawAsset link);

	public abstract GameAsset<?> getAssetForPath (FileHandle handle, boolean ignoreBroken);

	public abstract <U> GameAsset<U> getAssetForIdentifier (String identifier, GameAssetType type);

	public abstract NinePatch obtainNinePatch (GameAsset<Texture> gameAsset);


}
