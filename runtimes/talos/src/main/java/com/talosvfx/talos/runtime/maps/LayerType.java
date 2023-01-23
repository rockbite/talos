package com.talosvfx.talos.runtime.maps;

public enum LayerType {
	STATIC("Static"),
	DYNAMIC_ENTITY("Dynamic Entity");

	private final String readableName;

	LayerType (String readableName) {
		this.readableName = readableName;
	}
	@Override
	public String toString () {
		return readableName;
	}
}
