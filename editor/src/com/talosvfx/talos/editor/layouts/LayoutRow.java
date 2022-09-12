package com.talosvfx.talos.editor.layouts;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;

public class LayoutRow extends LayoutItem {

	private Array<LayoutItem> columns = new Array<>();

	public LayoutRow (Skin skin, LayoutGrid grid) {
		super(skin, grid);
	}

	public void addColumnContainer (LayoutItem layoutContainer) {
		columns.add(layoutContainer);
		addActor(layoutContainer);
	}

	public void removeColumnContainer (LayoutContent layoutContainer) {

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
}
