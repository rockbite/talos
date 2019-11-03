package com.rockbite.tools.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.rockbite.tools.talos.TalosMain;

public class CheckboxWidget extends PropertyWidget<Boolean> {

	private CheckBox checkBox;

	@Override
	public void refresh () {
		checkBox.setChecked(bondedProperty.getValue());
	}

	@Override
	public Actor getValueActor() {
		checkBox = new CheckBox("", TalosMain.Instance().getSkin());
		checkBox.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				((MutableProperty<Boolean>) bondedProperty).changed(checkBox.isChecked());
			}
		});

		return checkBox;
	}

	@Override
	public void configureForProperty (Property property) {
		super.configureForProperty(property);
		checkBox.setText(property.getPropertyName());
	}
}
