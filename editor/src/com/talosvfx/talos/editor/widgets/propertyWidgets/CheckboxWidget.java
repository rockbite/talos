package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.project2.SharedResources;

import java.util.function.Supplier;

public class CheckboxWidget extends PropertyWidget<Boolean> {

	private CheckBox checkBox;

	public CheckboxWidget() {
		super();
	}

	public CheckboxWidget(String name, Supplier<Boolean> supplier, ValueChanged<Boolean> valueChanged) {
		super(name, supplier, valueChanged);
	}

	@Override
	protected void addToContainer(Actor actor) {
		valueContainer.add().expandX();
		valueContainer.add(actor).right();
	}

	@Override
	public Actor getSubWidget() {
		checkBox = new CheckBox("", SharedResources.skin, "panel-checkbox");

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
		checkBox.removeListener(listener);
		checkBox.setChecked(value);
		checkBox.addListener(listener);
	}
}
