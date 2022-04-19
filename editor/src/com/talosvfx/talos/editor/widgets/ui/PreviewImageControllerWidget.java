/*******************************************************************************
 * Copyright 2019 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.talosvfx.talos.editor.widgets.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.rockbite.bongo.engine.systems.render.EngineDebugSystem;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.widgets.ui.common.SquareLabelButton;

public class PreviewImageControllerWidget extends Table {

	CheckBox gridCheckBox;
	CheckBox backgroundCheckBox;
	CheckBox axisCheckbox;
	TextField imageSizeField;
	TextField backgroundSizeField;
	ImageButton removeBackgroundButton;
	SquareLabelButton dimensionChangeButton;

	public PreviewImageControllerWidget (Skin skin) {
		setSkin(skin);
		setBackground(skin.getDrawable("seekbar-background"));

		defaults().pad(5);


		backgroundCheckBox = new CheckBox("bg", skin);
		add(backgroundCheckBox).left();

		TextField.TextFieldFilter filter = new TextField.TextFieldFilter() {
			@Override
			public boolean acceptChar (TextField textField, char c) {
				return Character.isDigit(c) || (c == '.' && !textField.getText().contains("."));
			}
		};

		Label imgLbl = new Label("Image: ", getSkin(), "small"); add(imgLbl).padRight(5f);
		imageSizeField = new TextField("", skin);
		imageSizeField.setTextFieldFilter(filter);
		add(imageSizeField).expandX().growX();

		removeBackgroundButton = new ImageButton(skin.getDrawable("ic-scale-del"));
		removeBackgroundButton.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				removeImage();
			}
		});
		add(removeBackgroundButton).height(40).pad(0);


		row();
		gridCheckBox = new CheckBox("gr", skin);
		gridCheckBox.setChecked(true);
		add(gridCheckBox).left();

		Label bgLbl = new Label("Grid: ", getSkin(), "small"); add(bgLbl).padRight(5f);
		backgroundSizeField = new TextField("1", skin);
		backgroundSizeField.setTextFieldFilter(filter);
		add(backgroundSizeField).expandX().growX();

		backgroundSizeField.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if(backgroundSizeField.getText().length() > 0) {
					gridSizeChanged(Float.parseFloat(backgroundSizeField.getText()));
				}
			}
		});

		row();
		axisCheckbox = new CheckBox("axis", skin);
		axisCheckbox.setChecked(false);
		add(axisCheckbox).colspan(3).left();

		axisCheckbox.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				final BongoPreview bongoPreview = TalosMain.Instance().UIStage().getInnerTertiumActor().getBongoPreview();
				final EngineDebugSystem system = bongoPreview.getWorld().getSystem(EngineDebugSystem.class);
				if (axisCheckbox.isChecked()) {
					system.setDrawAxis(true);
				} else {
					system.setDrawAxis(false);
				}
			}
		});

		dimensionChangeButton = new SquareLabelButton(skin, "3D");
		dimensionChangeButton.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				changeDimension();
			}
		});

		add(dimensionChangeButton).size(30).pad(0);
	}

	public void setImageWidth (float width) {
		imageSizeField.setText(String.valueOf(width));
	}

	public void setFieldOfWidth (float fieldOfWidth) {
		backgroundSizeField.setText(String.valueOf(fieldOfWidth));
	}

	public boolean isBackground () {
		return backgroundCheckBox.isChecked();
	}

	public boolean isGridVisible() {
		return gridCheckBox.isChecked();
	}

	public void removeImage () {

	}

	public void gridSizeChanged(float size) {

	}

	private void changeDimension() {
		TalosMain.Instance().UIStage().swapDimensions();
	}

	public void dimensionChanged (boolean isChangedToThreeDimension) {
		if (isChangedToThreeDimension) {
			dimensionChangeButton.setText("2D");
		} else {
			dimensionChangeButton.setText("3D");
		}
	}

	public float getImageWidth () {
		if (imageSizeField.getText().isEmpty()) {
			return 0;
		}

		return Float.parseFloat(imageSizeField.getText());
	}

	public float getPreviewBoxWidth () {
		if (backgroundSizeField.getText().isEmpty()) {
			return 0;
		}

		return Float.parseFloat(backgroundSizeField.getText());
	}

	public void setIsBackground(boolean isBackground) {
		backgroundCheckBox.setChecked(isBackground);
	}

	public void setGridVisible(boolean isGridVisible) {
		gridCheckBox.setChecked(isGridVisible);
	}

	public void setGridSize(float gridSize) {
		backgroundSizeField.setText(gridSize+"");
	}
}
