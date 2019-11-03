package com.rockbite.tools.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.wrappers.MutableProperty;
import com.rockbite.tools.talos.editor.wrappers.Property;

public class FloatWidget extends PropertyWidget<Float>{

	private Label descriptionLabel;
	private TextField valueChangeField;

	@Override
	public void refresh () {
		valueChangeField.setText(String.valueOf(bondedProperty.getValue()));
	}

	public FloatWidget () {
		super ();

		descriptionLabel = new Label("", TalosMain.Instance().getSkin());
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

		add(descriptionLabel).left();
		add(valueChangeField).growX();
	}

	@Override
	public void configureForProperty (Property property) {
		super.configureForProperty(property);
		descriptionLabel.setText(property.getPropertyName());
	}
}
