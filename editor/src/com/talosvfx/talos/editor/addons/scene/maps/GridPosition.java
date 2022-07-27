package com.talosvfx.talos.editor.addons.scene.maps;

public class GridPosition {
	public float x;
	public float y;

	public GridPosition() {}

	public GridPosition (float x, float y) {
		this.x = x;
		this.y = y;
	}

	public int getIntX () {
		return (int)x;
	}

	public int getIntY () {
		return (int)y;
	}
}
