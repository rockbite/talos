package com.talosvfx.talos.editor.addons.scene.maps;

import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;

public class StaticTile {

	GameAsset<?> staticTilesAsset;
	GridPosition gridPosition;

	public GameAsset<?> getStaticTilesAsset () {
		return staticTilesAsset;
	}

	public GridPosition getGridPosition () {
		return gridPosition;
	}
}
