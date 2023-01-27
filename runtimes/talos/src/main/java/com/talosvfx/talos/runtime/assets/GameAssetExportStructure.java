package com.talosvfx.talos.runtime.assets;

import com.badlogic.gdx.utils.Array;

public class GameAssetExportStructure {
	public String identifier;
	public String uuid;
	public GameAssetType type;
	public Array<String> relativePathsOfRawFiles = new Array<>();
}

