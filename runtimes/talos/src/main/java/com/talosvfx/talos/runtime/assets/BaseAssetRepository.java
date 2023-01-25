package com.talosvfx.talos.runtime.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import lombok.Getter;

import java.util.UUID;

public abstract class BaseAssetRepository {

	@Getter
	public static class AssetRepositoryCatalogueExportOptions {
		boolean forceExportAll = true;
		private FileHandle exportScriptHandle;
		private FileHandle exportPathHandle;

		public void loadFromPrefs (Preferences projectPrefs) {
			exportScriptHandle = Gdx.files.absolute(projectPrefs.getString("project.general.exportScript", ""));
			exportPathHandle = Gdx.files.absolute(projectPrefs.getString("project.general.exportPath", ""));
		}
	}
	public abstract void reloadGameAssetForRawFile (RawAsset link);

	public abstract GameAsset<?> getAssetForPath (FileHandle handle, boolean ignoreBroken);

	public abstract <U> GameAsset<U> getAssetForIdentifier (String identifier, GameAssetType type);

	public abstract <U> GameAsset<U> getAssetForUniqueIdentifier (UUID uuid, GameAssetType type);

	public abstract NinePatch obtainNinePatch (GameAsset<Texture> gameAsset);


}
