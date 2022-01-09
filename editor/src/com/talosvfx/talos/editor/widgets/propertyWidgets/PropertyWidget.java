package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.events.PropertyHolderEdited;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.widgets.ui.ActorCloneable;

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

	public PropertyWidget () {
		build("empty");
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
		Notifications.fireEvent(Notifications.obtainEvent(PropertyHolderEdited.class));
	}

	public void valueChanged(T value) {
		valueChanged.report(value);
	}

	protected boolean isFullSize() {
		return false;
	}

	public PropertyWidget clone()  {
		try {
			PropertyWidget widget = ClassReflection.newInstance(this.getClass());

			widget.supplier = this.supplier;
			widget.valueChanged = this.valueChanged;
			if(widget.propertyName != null) {
				widget.propertyName.setText(this.propertyName.getText());
			}
			return widget;

		} catch (ReflectionException e) {
			e.printStackTrace();
		}

		return null;
	}

	public void set(Supplier<T> supplier, ValueChanged<T> valueChanged) {
		this.supplier = supplier;
		this.valueChanged = valueChanged;
	}

	public void report(T value) {
		valueChanged.report(value);
	}
}
