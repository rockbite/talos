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

package com.rockbite.tools.talos.editor.widgets.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class PreviewImageControllerWidget extends Table {

	CheckBox backgroundCheckBox;
	TextField imageSizeField;
	TextField backgroundSizeField;
	ImageButton removeBackgroundButton;

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

		imageSizeField = new TextField("", skin);
		imageSizeField.setTextFieldFilter(filter);
		add(imageSizeField).width(70).left();

		backgroundSizeField = new TextField("10", skin);
		backgroundSizeField.setTextFieldFilter(filter);
		add(backgroundSizeField).width(70);

		removeBackgroundButton = new ImageButton(skin.getDrawable("ic-scale-del"));
		removeBackgroundButton.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				removeImage();
			}
		});
		add(removeBackgroundButton).height(40).pad(0);
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

	public void removeImage () {

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
}
