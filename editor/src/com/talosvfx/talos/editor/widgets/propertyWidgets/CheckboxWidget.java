package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.common.zoomWidgets.CheckboxWithZoom;

import com.talosvfx.talos.runtime.utils.Supplier;

public class CheckboxWidget extends PropertyWidget<Boolean> {

	private CheckboxWithZoom checkBox;

	protected CheckboxWidget () {}

	public CheckboxWidget(String name, Supplier<Boolean> supplier, ValueChanged<Boolean> valueChanged, Object parent) {
		super(name, supplier, valueChanged, parent);
	}

	@Override
	protected void addToContainer(Actor actor) {
		valueContainer.add().expandX();
		valueContainer.add(actor).right();
	}

	@Override
	public Actor getSubWidget() {
		checkBox = new CheckboxWithZoom("", SharedResources.skin, "panel-checkbox");

		listener = new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				callValueChanged(checkBox.isChecked());
			}
		};
		checkBox.addListener(listener);

		return checkBox;
	}

	@Override
	public void updateWidget(Boolean value) {
		if(value == null) return;
		checkBox.setProgrammaticChangeEvents(false);
		checkBox.setChecked(value);
	}
}
