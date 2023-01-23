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

	public RawAsset (FileHandle file) {
		this.handle = file;
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
