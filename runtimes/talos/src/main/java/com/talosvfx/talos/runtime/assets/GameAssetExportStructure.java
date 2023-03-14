package com.talosvfx.talos.runtime.assets;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;

public class GameAssetExportStructure {
	public String identifier;
	public String uuid;
	public GameAssetType type;
	public Array<String> relativePathsOfRawFiles = new Array<>();
	public ObjectSet<String> dependentGameAssets = new ObjectSet<>();
}

