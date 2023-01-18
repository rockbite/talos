package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Align;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.common.zoomWidgets.LabelWithZoom;

import java.util.function.Supplier;

public class LabelWidget extends PropertyWidget<String> {

	private LabelWithZoom propertyValue;

	public LabelWidget() {
		super();
	}

	public LabelWidget(String name, Supplier<String> supplier) {
		super(name, supplier, null);
	}

	@Override
	public Actor getSubWidget() {
		propertyValue = new LabelWithZoom("", SharedResources.skin);
		propertyValue.setEllipsis(true);
		propertyValue.setAlignment(Align.right);

		return propertyValue;
	}

	@Override
	public void updateWidget(String value) {
		if(value == null) {
			propertyValue.setText("-");
		} else {
			propertyValue.setText(value);
		}
	}
}
