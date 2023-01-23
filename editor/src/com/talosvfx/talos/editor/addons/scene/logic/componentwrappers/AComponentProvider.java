package com.talosvfx.talos.editor.addons.scene.logic.componentwrappers;

import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.talosvfx.talos.runtime.scene.components.AComponent;

public abstract class AComponentProvider<T extends AComponent> extends ComponentPropertyProvider<T> {

	public AComponentProvider (T component) {
		super(component);
	}

	@Override
	public Class<? extends IPropertyProvider> getType () {
		return getClass();
	}
}
