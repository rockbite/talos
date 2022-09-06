package com.talosvfx.talos.editor.layouts;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;

public class LayoutRow extends LayoutItem {

	private Array<LayoutItem> columns = new Array<>();

	public LayoutRow (Skin skin, LayoutGrid grid) {
		super(skin, grid);
	}

	public void addColumnContainer (LayoutItem newLayoutContent, boolean left) {
		addColumnContainer(newLayoutContent, left, null);
	}

	public void addColumnContainer (LayoutItem newLayoutContent, boolean left, @Null LayoutItem relative) {
		if (relative != null) {
			//We are relative to some other actor
			int idxRelative = columns.indexOf(relative, true);
			if (left) {
//				idxRelative -= 0; Don't move it
			} else {
				idxRelative += 1;
			}

			columns.insert(idxRelative, newLayoutContent);

		} else {
			if (left) {
				columns.insert(0, newLayoutContent);
			} else {
				columns.add(newLayoutContent);
			}
		}

		addActor(newLayoutContent);
	}


	@Override
	public void layout () {
		super.layout();

		int size = columns.size;
		if (size == 0)
			return;

		float widthPerColumn = getWidth() / size;

		float debugBuffer = 0;

		float counterX = 0;
		//Evenly distribute for now
		for (LayoutItem row : columns) {
			row.setBounds(counterX + debugBuffer, debugBuffer, widthPerColumn - (debugBuffer * 2), getHeight() - (debugBuffer * 2f));
			counterX += widthPerColumn;
		}
	}

	@Override
	public void removeItem (LayoutContent content) {
		columns.removeValue(content,true);
		removeActor(content);
		invalidate();
	}

	@Override
	public void exchange (LayoutContent target, LayoutItem newColumn) {
		removeActor(target);

		int idx = columns.indexOf(target, true);

		columns.set(idx, newColumn);
		addActor(newColumn);
	}

	@Override
	public boolean isEmpty () {
		return columns.isEmpty();
	}
}
