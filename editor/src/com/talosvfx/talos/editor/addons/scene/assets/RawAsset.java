package com.talosvfx.talos.editor.addons.scene.assets;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.utils.AMetadata;

/**
 * Raw asset is a single file, tracked by meta data
 */
public class RawAsset {

	FileHandle handle;
	AMetadata metaData;

	Array<GameAsset> gameAssetReferences = new Array<>();

	public RawAsset (FileHandle file) {
		this.handle = file;
	}

	@Override
	public String toString () {
		return handle.path() + " " + metaData.getClass().getSimpleName();
	}
}
