package com.talosvfx.talos.editor.addons.scene.maps;

import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;

public class StaticTile {

	GameAsset<?> staticTilesAsset;
	GridPosition gridPosition;

	public StaticTile() { }

	public StaticTile(GameAsset<?> gameAsset, GridPosition gridPosition) {
		staticTilesAsset = gameAsset;
		this.gridPosition = gridPosition;
	}

	public GameAsset<?> getStaticTilesAsset () {
		return staticTilesAsset;
	}

	public GridPosition getGridPosition () {
		return gridPosition;
	}
}
