package com.talosvfx.talos.runtime.maps;

import com.badlogic.gdx.math.Vector2;
import com.talosvfx.talos.runtime.scene.GameObject;import com.talosvfx.talos.runtime.maps.GridPosition;
import com.talosvfx.talos.runtime.maps.StaticTile;

public class TileGameObjectProxy extends GameObject {

	public StaticTile staticTile;

	public TileGameObjectProxy () {}

	public boolean containsPoint (Vector2 worldPos) {
		GridPosition gridPosition = staticTile.getGridPosition();
		if (worldPos.x >= gridPosition.x && worldPos.x <= gridPosition.x + 1) {
			if (worldPos.y >= gridPosition.y && worldPos.y <= gridPosition.y + 1) {
				return true;
			}
		}
		return false;
	}
}
