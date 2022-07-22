package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
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

	private boolean hasName = true;


	public void toggleHide (boolean hidden) {
		//Check if we are in a cell
		if (getParent() instanceof Table) {
			Cell<PropertyWidget<T>> cell = ((Table)getParent()).getCell(this);
			if (cell != null) {
				if (hidden) {
					setVisible(false);
					cell.height(0);
					((Table)getParent()).invalidateHierarchy();
				} else {
					setVisible(true);
					cell.height(Value.prefHeight);
					((Table)getParent()).invalidateHierarchy();
				}
			}
		}
	}

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
			hasName = true;
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
			hasName = false;
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
		fire(new ChangeListener.ChangeEvent());
		valueChanged.report(value);
	}

	protected boolean isFullSize() {
		return !hasName;
	}

	public PropertyWidget clone()  {
		try {
			PropertyWidget widget = ClassReflection.newInstance(this.getClass());

			widget.supplier = this.supplier;
			widget.valueChanged = this.valueChanged;
			if (widget.propertyName != null && this.propertyName != null) {
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
