package com.talosvfx.talos.editor.layouts;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.talosvfx.talos.editor.utils.CursorUtil;

public class LayoutResizeWidget extends Table {

	private final Skin skin;
	private final LayoutGrid layoutGrid;


	private LayoutItem responder;
	private boolean entered;

	private Drawable noFocus;
	private Drawable focus;

	public LayoutResizeWidget (Skin skin, LayoutGrid layoutGrid, LayoutItem responder) {
		this.skin = skin;
		this.layoutGrid = layoutGrid;
		this.responder = responder;

		noFocus = skin.newDrawable("white", Color.valueOf("333333ff"));
		focus = skin.newDrawable("white", Color.valueOf("666666ff"));

		setBackground(noFocus);

		ClickListener listener = new ClickListener() {

			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				entered = true;

				setBackground(focus);
				super.enter(event, x, y, pointer, fromActor);
			}

			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				entered = false;

				setBackground(noFocus);
				super.exit(event, x, y, pointer, toActor);
			}

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


	@Override
	public void act(float delta) {
		super.act(delta);
		if (entered) {
			if (responder instanceof LayoutColumn) {
				CursorUtil.setDynamicModeCursor(CursorUtil.CursorType.MOVE_VERTICALLY);
			} else if (responder instanceof LayoutRow) {
				CursorUtil.setDynamicModeCursor(CursorUtil.CursorType.MOVE_HORIZONTALLY);
			}
		}
	}
}
