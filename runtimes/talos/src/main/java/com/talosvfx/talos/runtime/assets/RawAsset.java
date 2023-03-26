package com.talosvfx.talos.runtime.assets;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

/**
 * Raw asset is a single file, tracked by meta data
 */
public class RawAsset {

	public FileHandle handle;
	public AMetadata metaData;

	public Array<GameAsset> gameAssetReferences = new Array<>();

	public boolean shouldExport = true;

	public RawAsset (FileHandle file) {
		this.handle = file;
	}
	public RawAsset copy () {
		RawAsset rawAsset = new RawAsset(handle);
		rawAsset.metaData = metaData;
		rawAsset.shouldExport = shouldExport;
		rawAsset.gameAssetReferences.addAll(gameAssetReferences);
		return rawAsset;
	}
	@Override
	public String toString () {
		if (metaData == null) {
			return "Oopsi";
		}
		Class<? extends AMetadata> aClass = metaData.getClass();
		if (aClass == null) {
			return "Oopsi";
		}
		return handle.path() + " " + aClass.getSimpleName();
	}


}
