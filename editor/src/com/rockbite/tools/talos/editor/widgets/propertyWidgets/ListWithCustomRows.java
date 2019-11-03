package com.rockbite.tools.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Selection;
import com.badlogic.gdx.utils.Array;
import com.rockbite.tools.talos.editor.addons.bvb.GlobalValueWidget;


public class ListWithCustomRows<T extends ListCustomRow> extends Table {

	private Array<T> items = new Array<>();
	Selection<GlobalValueWidget> currentSelection = new Selection<>();
	private T currentSelectedItem;

	private Vector2 temp = new Vector2();

	public ListWithCustomRows (Skin skin) {
		setSkin(skin);
		configureListener();
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

	private void configureListener () {
		addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				for (T item : items) {
					temp.set(0, 0);
					Vector2 vector2 = item.localToStageCoordinates(temp);

					if (item.hit(temp.x, temp.y, true) != null) {
						if (item == currentSelectedItem) {
							currentSelectedItem.setSelected(false);
							currentSelectedItem = null;
						} else {
							item.setSelected(true);
							currentSelectedItem = item;
						}
						break;
					}
				}
			}
		});
	}

	public Array<T> getItems () {
		return items;
	}

	private void reconstruct () {
		clearChildren();
		for (T item : items) {
			add(item).growX();
			row();
		}
	}
}
