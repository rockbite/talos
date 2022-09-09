package com.talosvfx.talos.editor.layouts;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;

public class LayoutColumn extends LayoutItem {

	Array<LayoutItem> rows = new Array<>();

	public LayoutColumn (Skin skin, LayoutGrid grid) {
		super(skin, grid);
	}

	public enum LayoutDirection {
		UP,RIGHT,DOWN,LEFT
	}

	public void addContainer (LayoutItem containerToPlace) {
		rows.add(containerToPlace);
		addActor(containerToPlace);
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
}
