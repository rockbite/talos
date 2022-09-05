package com.talosvfx.talos.editor.layouts;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class LayoutItem extends WidgetGroup {

	protected final LayoutGrid grid;

	private final Drawable background;

	public LayoutItem (Skin skin, LayoutGrid grid) {
		this.grid = grid;

		background = skin.newDrawable("white", MathUtils.random(), MathUtils.random(), MathUtils.random(), 0.8f);
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		background.draw(batch, getX(), getY(), getWidth(), getHeight());
		super.draw(batch, parentAlpha);
	}


}
