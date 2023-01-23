package com.talosvfx.talos.editor.addons.scene.logic.componentwrappers;

import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.talosvfx.talos.runtime.scene.components.AComponent;

public abstract class ComponentPropertyProvider<T extends AComponent> implements IPropertyProvider {

	protected final T component;

	public ComponentPropertyProvider (T component) {
		this.component = component;
	}

}
