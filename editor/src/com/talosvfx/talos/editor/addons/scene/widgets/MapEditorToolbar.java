package com.talosvfx.talos.editor.addons.scene.widgets;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.widgets.ui.common.SquareButton;

public class MapEditorToolbar extends Table {

	public MapEditorToolbar (Skin skin) {
		super(skin);

		setFillParent(true);
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
	}

	@Override
	public boolean remove () {
		return super.remove();
	}

	public void build () {

		clearChildren();

		top();

		Table toolbar = new Table();
		add(toolbar).top().expandX().expandX();

		toolbar.defaults().pad(5);

		toolbar.setBackground(getSkin().newDrawable("button-main-menu"));

		SquareButton paint = new SquareButton(getSkin(), getSkin().getDrawable("brush_icon"), true);
		SquareButton erase = new SquareButton(getSkin(), getSkin().getDrawable("eraser_icon"), true);

		ButtonGroup<SquareButton> buttonButtonGroup = new ButtonGroup<>();
		buttonButtonGroup.setMaxCheckCount(1);
		buttonButtonGroup.setMinCheckCount(0);
		buttonButtonGroup.add(paint, erase);


		paint.addListener(new ClickListener() {

			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				event.cancel();
				return super.touchDown(event, x, y, pointer, button);
			}

			@Override
			public void clicked (InputEvent event, float x, float y) {

				super.clicked(event, x, y);

				SceneEditorWorkspace.getInstance().mapEditorState.setErasing(false);
				SceneEditorWorkspace.getInstance().mapEditorState.setPainting(false);

				SceneEditorWorkspace.getInstance().mapEditorState.setPainting(paint.isChecked());

				//Lock gizmos and also lock seleection
				if (paint.isChecked() || erase.isChecked()) {
					SceneEditorWorkspace.getInstance().lockGizmos();
				} else {
					SceneEditorWorkspace.getInstance().unlockGizmos();
				}
			}
		});
		erase.addListener(new ClickListener() {

			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				event.cancel();
				return super.touchDown(event, x, y, pointer, button);
			}


			@Override
			public void clicked (InputEvent event, float x, float y) {
				super.clicked(event, x, y);

				SceneEditorWorkspace.getInstance().mapEditorState.setErasing(false);
				SceneEditorWorkspace.getInstance().mapEditorState.setPainting(false);

				SceneEditorWorkspace.getInstance().mapEditorState.setErasing(erase.isChecked());

				//Lock gizmos and also lock seleection
				if (paint.isChecked() || erase.isChecked()) {
					SceneEditorWorkspace.getInstance().lockGizmos();
				} else {
					SceneEditorWorkspace.getInstance().unlockGizmos();
				}
			}
		});

		toolbar.add(paint);
		toolbar.add(erase);

		//Add the buttons

	}
}
