package com.talosvfx.talos.editor.layouts;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class LayoutResizeWidget extends Table {

	private final Skin skin;
	private final com.talosvfx.talos.editor.layouts.LayoutGrid layoutGrid;

	public LayoutResizeWidget (Skin skin, LayoutGrid layoutGrid, LayoutItem responder) {
		this.skin = skin;
		this.layoutGrid = layoutGrid;

		setBackground(skin.newDrawable("white", 0, 0, 0, 1f));

		ClickListener listener = new ClickListener() {

			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				responder.touchedTownResizeWidget(LayoutResizeWidget.this, event, x, y, pointer);

				return super.touchDown(event, x, y, pointer, button);
			}

			@Override
			public void touchDragged (InputEvent event, float x, float y, int pointer) {
				responder.draggedResizeWidget(LayoutResizeWidget.this, event, x, y, pointer);
				super.touchDragged(event, x, y, pointer);
			}
		};
		addListener(listener);

		setTouchable(Touchable.enabled);
	}
}
