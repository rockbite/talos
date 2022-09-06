package com.talosvfx.talos.editor.layouts;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;

public class LayoutColumn extends LayoutItem {

	Array<LayoutItem> rows = new Array<>();

	public LayoutColumn (Skin skin, LayoutGrid grid) {
		super(skin, grid);
	}


	public enum LayoutDirection {
		UP,RIGHT,DOWN,LEFT
	}

	public void addRowContainer (LayoutItem newLayoutContent, boolean up) {
		addRowContainer(newLayoutContent, up, null);
	}

	public void addRowContainer (LayoutItem newLayoutContent, boolean up, @Null LayoutContent relative) {

		if (relative != null) {
			//We are relative to some other actor
			int idxRelative = rows.indexOf(relative, true);
			if (!up) {
//				idxRelative -= 0; Don't move it
			} else {
				idxRelative += 1;
			}

			rows.insert(idxRelative, newLayoutContent);

		} else {
			if (!up) {
				rows.insert(0, newLayoutContent);
			} else {
				rows.add(newLayoutContent);
			}
		}

		addActor(newLayoutContent);
	}


	@Override
	public void layout () {
		super.layout();

		int size = rows.size;
		if (size == 0) return;

		float heightPerRow = getHeight()/size;

		float counterY = 0;
		//Evenly distribute for now
		for (LayoutItem row : rows) {
			row.setBounds(0, counterY, getWidth(), heightPerRow);
			counterY += heightPerRow;
		}
	}

	@Override
	public void removeItem (LayoutContent content) {
		rows.removeValue(content, true);
		removeActor(content);
		invalidate();
	}

	@Override
	public void exchange (LayoutContent target, LayoutItem newColumn) {
		removeActor(target);

		int idx = rows.indexOf(target, true);

		rows.set(idx, newColumn);
		addActor(newColumn);
	}

	@Override
	public boolean isEmpty () {
		return rows.isEmpty();
	}
}
