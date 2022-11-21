package com.talosvfx.talos.editor.layouts;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kotcrab.vis.ui.widget.VisLabel;

public class LayoutContent extends LayoutItem {


	public LayoutContent (Skin skin, LayoutGrid grid) {
		super(skin, grid);

		VisLabel label = new VisLabel("TestContent: " + MathUtils.random(10));

		Table innerContents = new Table();

		innerContents.top().left();
		innerContents.add(label);

		innerContents.setFillParent(true);
		addActor(innerContents);
	}

}
