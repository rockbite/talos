package com.rockbite.tools.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.ui.Table;

public abstract class ListCustomRow extends Table {

	boolean isSelected = false;

	public  void setSelected (boolean isSelected) {
		this.isSelected = isSelected;
	}

	public boolean isSelected () {
		return isSelected;
	}

}
