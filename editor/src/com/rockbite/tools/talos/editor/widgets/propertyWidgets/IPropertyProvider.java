package com.rockbite.tools.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.utils.Array;

public interface IPropertyProvider {
	public Array<Property> getListOfProperties ();
	public String getPropertyBoxTitle ();
}
