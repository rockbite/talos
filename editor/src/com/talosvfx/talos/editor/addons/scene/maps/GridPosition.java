package com.talosvfx.talos.editor.addons.scene.maps;

import com.badlogic.gdx.math.MathUtils;

import java.util.Objects;

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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof GridPosition)) return false;
		GridPosition that = (GridPosition) o;
		return that.getIntX() == getIntX() && that.getIntY() == getIntY();
	}

	@Override
	public int hashCode() {
		return Objects.hash(getIntX(), getIntY());
	}
}
