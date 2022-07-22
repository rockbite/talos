package com.talosvfx.talos.editor.addons.scene.maps;

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
