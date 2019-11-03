package com.rockbite.tools.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.rockbite.tools.talos.TalosMain;

public class LabelWidget extends PropertyWidget<String> {

	private Label propertyValue;

	@Override
	public void refresh () {
		propertyValue.setText(bondedProperty.getValue());
	}

	public LabelWidget () {
		super();


	}

	@Override
	public Actor getValueActor() {
		propertyValue = new Label("", TalosMain.Instance().getSkin());
		propertyValue.setAlignment(Align.right);

		return propertyValue;
	}
}
