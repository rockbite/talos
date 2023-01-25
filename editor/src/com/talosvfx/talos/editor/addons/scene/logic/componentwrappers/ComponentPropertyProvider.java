package com.talosvfx.talos.editor.addons.scene.logic.componentwrappers;

import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.talosvfx.talos.runtime.scene.components.AComponent;
import lombok.Getter;

public abstract class ComponentPropertyProvider<T extends AComponent> implements IPropertyProvider {
	@Getter
	protected final T component;

	public ComponentPropertyProvider (T component) {
		this.component = component;
	}

}
