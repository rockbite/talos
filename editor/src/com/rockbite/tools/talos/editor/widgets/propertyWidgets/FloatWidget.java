package com.rockbite.tools.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.rockbite.tools.talos.TalosMain;

public class FloatWidget extends PropertyWidget<Float>{

	private TextField valueChangeField;

	@Override
	public void refresh () {
		valueChangeField.setText(String.valueOf(bondedProperty.getValue()));
	}

	public FloatWidget () {
		super ();
	}

	@Override
	public Actor getValueActor() {
		valueChangeField = new TextField("", TalosMain.Instance().getSkin());
		valueChangeField.setTextFieldFilter(new TextField.TextFieldFilter() {
			@Override
			public boolean acceptChar (TextField textField, char c) {
				return Character.isDigit(c) || (c == '.' && !textField.getText().contains("."));
			}
		});
		valueChangeField.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				((MutableProperty<Float>) bondedProperty).changed(Float.parseFloat(valueChangeField.getText()));
			}
		});

		return valueChangeField;
	}
}
