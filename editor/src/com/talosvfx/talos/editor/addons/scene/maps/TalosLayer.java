package com.talosvfx.talos.editor.addons.scene.maps;

import com.talosvfx.talos.editor.widgets.propertyWidgets.ValueProperty;

public class TalosLayer {



	enum LayerType {
		STATIC,
		DYNAMIC_ENTITY
	}

	private String name;
	private LayerType type;

	protected TalosLayer () {}

	public TalosLayer (String name) {
		this.name = name;
		this.type = LayerType.STATIC;
	}
	public String getName () {
		return name;
	}

	public void setName (String newName) {
		this.name = newName;
	}

	@Override
	public String toString () {
		return name + " - " + type.toString();
	}
}
