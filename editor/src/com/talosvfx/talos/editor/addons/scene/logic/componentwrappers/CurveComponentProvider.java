package com.talosvfx.talos.editor.addons.scene.logic.componentwrappers;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.widgets.propertyWidgets.ButtonPropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.CheckboxWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.LabelWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.runtime.scene.components.CurveComponent;

import java.util.function.Supplier;

public class CurveComponentProvider extends AComponentProvider<CurveComponent> {

	public CurveComponentProvider (CurveComponent component) {
		super(component);
	}

	@Override
	public Array<PropertyWidget> getListOfProperties () {
		Array<PropertyWidget> properties = new Array<>();

		properties.add(new LabelWidget("segments", new Supplier<String>() {
			@Override
			public String get () {
				return component.getNumSegments() + "";
			}
		}, component));

		properties.add(new LabelWidget("points", new Supplier<String>() {
			@Override
			public String get () {
				return component.points.size + "";
			}
		}, component));

		ButtonPropertyWidget<String> cleanButton = new ButtonPropertyWidget<String>("Create New", new ButtonPropertyWidget.ButtonListener() {
			@Override
			public void clicked (ButtonPropertyWidget widget) {
				component.setToNew();
			}
		});

		properties.add(cleanButton);

		CheckboxWidget isClosedWidget = new CheckboxWidget("Toggle Closed", new Supplier<Boolean>() {
			@Override
			public Boolean get () {
				return component.isClosed;
			}
		}, new PropertyWidget.ValueChanged<Boolean>() {
			@Override
			public void report (Boolean value) {
				if (component.isClosed != value) {
					component.setClosedState(value); //TODO THIS SHOULD BE WIDGET FACTORY AND USE REFLECTION METHOD OVERRIDE
				}
			}
		}, component);

		properties.add(cleanButton);
		properties.add(isClosedWidget);

		CheckboxWidget autoSetWidget = new CheckboxWidget("Automatic Control", new Supplier<Boolean>() {
			@Override
			public Boolean get () {
				return component.automaticControl;
			}
		}, new PropertyWidget.ValueChanged<Boolean>() {
			@Override
			public void report (Boolean value) {
				if (component.automaticControl != value) {
					component.automaticControl = value;
					if (component.automaticControl) {
						component.autoSetAllControlPoints();
					}
				}
			}
		}, component);
		properties.add(autoSetWidget);

		return properties;
	}

	@Override
	public String getPropertyBoxTitle () {
		return "Curve Component";
	}

	@Override
	public int getPriority () {
		return 2;
	}
}
