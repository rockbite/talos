package com.rockbite.tools.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.wrappers.Property;

public class LabelWidget extends PropertyWidget<String> {

	private Label propertyName;
	private Label propertyValue;

	@Override
	public void refresh () {
		propertyValue.setText(bondedProperty.getValue());
	}

	public LabelWidget () {
		super();

		propertyName = new Label("", TalosMain.Instance().getSkin());
		propertyValue = new Label("", TalosMain.Instance().getSkin());

		add(propertyName).left();
		propertyName.setAlignment(Align.left);
		propertyValue.setAlignment(Align.right);
		add(propertyValue).right().expandX();
	}

	@Override
	public void configureForProperty (Property property) {
		super.configureForProperty(property);
		propertyName.setText(property.getPropertyName() + ": ");
	}
}
