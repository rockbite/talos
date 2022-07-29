package com.talosvfx.talos.editor.addons.scene.maps;

import com.badlogic.gdx.math.MathUtils;

public class GridPosition {
	public float x;
	public float y;

	public GridPosition() {}

	public GridPosition (float x, float y) {
		this.x = x;
		this.y = y;
	}

	public int getIntX () {
		return MathUtils.floor(x);
	}

	public int getIntY () {
		return MathUtils.floor(y);
	}
}
