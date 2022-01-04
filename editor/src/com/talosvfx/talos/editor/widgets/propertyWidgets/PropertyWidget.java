package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.talosvfx.talos.TalosMain;

import java.util.function.Supplier;

public abstract class PropertyWidget<T> extends Table {

	protected Label propertyName;
	protected Table valueContainer;
	protected T value;

	ChangeListener listener;

	private Supplier<T> supplier;
	private ValueChanged<T> valueChanged;

	public interface ValueChanged<T> {
		void report(T value);
	}

	public PropertyWidget (Supplier<T> supplier, ValueChanged<T> valueChanged) {
		this(null, supplier, valueChanged);
	}

	public PropertyWidget (String name, Supplier<T> supplier, ValueChanged<T> valueChanged) {
		this.supplier = supplier;
		this.valueChanged = valueChanged;
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

	public T getValue() {
		return supplier.get();
	}

	public Actor getSubWidget() {
		return null;
	}

	public void updateValue() {
		this.value = supplier.get();
		updateWidget(value);
	}

	public abstract void updateWidget(T value);


	protected void callValueChanged (T value) {
		valueChanged(value);
		TalosMain.Instance().ProjectController().setDirty();
	}

	public void valueChanged(T value) {
		valueChanged.report(value);
	}

	protected boolean isFullSize() {
		return false;
	}
}
