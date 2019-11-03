package com.rockbite.tools.talos.editor.wrappers;

import com.badlogic.gdx.utils.Array;

public interface IPropertyProvider {
	public Array<Property> getListOfProperties ();
	public String getTitle ();
}
