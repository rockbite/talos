package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.talosvfx.talos.TalosMain;

public abstract class PropertyWidget<T> extends Table {

	protected Label propertyName;
	protected Table valueContainer;
	protected T value;

	ChangeListener listener;

	public PropertyWidget () {
		build(null);
	}

	public PropertyWidget (String name) {
		build(name);
	}

	protected void build(String name) {
		if(name != null) {

			propertyName = new Label(name + ":", TalosMain.Instance().getSkin());
			propertyName.setAlignment(Align.left);
			valueContainer = new Table();

			if (isFullSize()) {
				add(propertyName).left().growX();
				row();
				add(valueContainer).growX();
			} else {
				add(propertyName).left();
				add(valueContainer).right().expandX().minWidth(170);
			}

			addToContainer(getSubWidget());
		} else {
			add(getSubWidget()).growX();
		}
	}

	protected void addToContainer(Actor actor) {
		valueContainer.add(actor).growX().width(0).right();
	}

	public abstract T getValue();

	public abstract Actor getSubWidget();

	public void updateValue() {
		this.value = getValue();
		updateWidget(value);
	}

	public abstract void updateWidget(T value);


	protected void callValueChanged (T value) {
		valueChanged(value);
		TalosMain.Instance().ProjectController().setDirty();
	}

	public void valueChanged(T value) {
		// do no thing
	}

	protected boolean isFullSize() {
		return false;
	}
}
