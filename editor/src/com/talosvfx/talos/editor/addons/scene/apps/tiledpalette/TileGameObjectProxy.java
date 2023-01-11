package com.talosvfx.talos.editor.addons.scene.apps.tiledpalette;

import com.badlogic.gdx.math.Vector2;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.maps.GridPosition;
import com.talosvfx.talos.editor.addons.scene.maps.StaticTile;

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
