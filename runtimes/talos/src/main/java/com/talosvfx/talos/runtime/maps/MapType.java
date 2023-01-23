package com.talosvfx.talos.runtime.maps;

public enum MapType {
	ORTHOGRAPHIC_TOPDOWN("Orthographic Top down");

	private final String displayName;

	MapType (String name) {
		this.displayName = name;
	}

	@Override
	public String toString () {
		return displayName;
	}
}
