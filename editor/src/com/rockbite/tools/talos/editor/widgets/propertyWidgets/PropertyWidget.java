package com.rockbite.tools.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.rockbite.tools.talos.editor.wrappers.Property;

public abstract class PropertyWidget<T> extends Table {

	protected Property<T> bondedProperty;
	protected abstract void refresh();

	public PropertyWidget () {}

	public void configureForProperty (Property property) {
		this.bondedProperty = property;
	}
}
