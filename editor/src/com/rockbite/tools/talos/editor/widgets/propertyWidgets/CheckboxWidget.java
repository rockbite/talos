package com.rockbite.tools.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.wrappers.MutableProperty;
import com.rockbite.tools.talos.editor.wrappers.Property;

public class CheckboxWidget extends PropertyWidget<Boolean> {
	private CheckBox checkBox;

	@Override
	protected void refresh () {
		checkBox.setChecked(bondedProperty.getValue());
	}

	public CheckboxWidget() {
		super();

		left();

		checkBox = new CheckBox("", TalosMain.Instance().getSkin());
		checkBox.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				((MutableProperty<Boolean>) bondedProperty).changed(checkBox.isChecked());
			}
		});

		add(checkBox).expandX();
	}

	@Override
	public void configureForProperty (Property property) {
		super.configureForProperty(property);
		checkBox.setText(property.getPropertyName());
	}
}
