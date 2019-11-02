package com.rockbite.tools.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;


public class ListWithCustomRows<T extends ListCustomRow> extends Table {

	private Array<T> items = new Array();
	private T currentSelectedItem;

	public ListWithCustomRows (Skin skin) {
		setSkin(skin);
	}

	public void addItem (T item) {
		items.add(item);
		reconstruct();
	}

	public void setItems (Array<T> newItems) {
		items.clear();
		items.addAll(newItems);
		reconstruct();
	}

	public void removeSelected () {
		if (currentSelectedItem != null) {
			items.removeValue(currentSelectedItem, true);
			reconstruct();
		}
	}

	private void reconstruct () {
		clearChildren();
		for (T item : items) {
			add(item).growX();
			row();
		}
	}
}
