package com.talosvfx.talos.runtime.scene.utils;

import com.badlogic.gdx.math.Vector2;

public class TransformSettings {
	public float gridSizeX = 1;
	public float gridSizeY = 1;

	public float offsetX = 0;
	public float offsetY = 0;

	public float transformOffsetX = 0;
	public float transformOffsetY = 0;

	public void setOffset (float storedX, float storedY) {
		this.offsetX = storedX;
		this.offsetY = storedY;
	}

	public void setStoredTransformOffset (Vector2 position) {
		this.transformOffsetX = position.x;
		this.transformOffsetY = position.y;
	}
}
