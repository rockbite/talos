package com.talosvfx.talos.runtime.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.talosvfx.talos.runtime.RuntimeContext;
import com.talosvfx.talos.runtime.graphics.NineSlice;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

import static com.badlogic.gdx.graphics.g2d.TextureAtlas.*;

public abstract class BaseAssetRepository {
	public static final UUID missingUUID = new UUID(-1, -1);

	@Getter@Setter
	private RuntimeContext.TalosContext talosContext;

	@Getter
	public static class AssetRepositoryCatalogueExportOptions {
		boolean forceExportAll = true;
		private FileHandle exportScriptHandle;
		private FileHandle scriptBinaryHandle;
		private FileHandle exportPathHandle;

		public void loadFromPrefs (Preferences projectPrefs) {
			String exportScriptPath = projectPrefs.getString("project.general.exportScript", null);
			if (exportScriptPath != null) {
				exportScriptHandle = Gdx.files.absolute(exportScriptPath);
			}
			exportPathHandle = Gdx.files.absolute(projectPrefs.getString("project.general.exportPath", ""));
			String binaryPath = projectPrefs.getString("project.general.scriptBinary", null);
			if (binaryPath != null) {
				scriptBinaryHandle = Gdx.files.absolute(binaryPath);
			}
		}
	}
	public abstract void reloadGameAssetForRawFile (RawAsset link);

	public abstract GameAsset<?> getAssetForPath (FileHandle handle, boolean ignoreBroken);

	public abstract <U> GameAsset<U> getAssetForIdentifier (String identifier, GameAssetType type);

	public abstract boolean isAssetLoadedForIdentifier (String identifier, GameAssetType type);

	public abstract void unloadAsset (GameAsset<?> gameAsset);

	public abstract <U> GameAsset<U> getAssetForUniqueIdentifier (UUID uuid, GameAssetType type);

	public abstract NineSlice obtainNinePatch (GameAsset<AtlasSprite> gameAsset);


}
