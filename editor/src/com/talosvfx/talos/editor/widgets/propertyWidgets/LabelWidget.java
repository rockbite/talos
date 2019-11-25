package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.talosvfx.talos.TalosMain;

public abstract class LabelWidget extends PropertyWidget<String> {

	private Label propertyValue;

	public LabelWidget(String name) {
		super(name);
	}

	@Override
	public Actor getSubWidget() {
		propertyValue = new Label("", TalosMain.Instance().getSkin());
		propertyValue.setWidth(170);
		propertyValue.setEllipsis(true);
		propertyValue.setAlignment(Align.right);

		return propertyValue;
	}

	@Override
	public void updateWidget(String value) {
		propertyValue.setText(value);
	}
}
