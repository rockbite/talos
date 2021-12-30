package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.talosvfx.talos.TalosMain;

public abstract class CheckboxWidget extends PropertyWidget<Boolean> {

	private CheckBox checkBox;

	public CheckboxWidget(String name) {
		super(name);
	}


	@Override
	protected void addToContainer(Actor actor) {
		valueContainer.add().expandX();
		valueContainer.add(actor).right();
	}

	@Override
	public Actor getSubWidget() {
		checkBox = new CheckBox("", TalosMain.Instance().getSkin(), "panel-checkbox");

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
		checkBox.removeListener(listener);
		checkBox.setChecked(value);
		checkBox.addListener(listener);
	}
}
