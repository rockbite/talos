package com.talosvfx.talos.editor.addons.scene.logic.componentwrappers;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.WidgetFactory;
import com.talosvfx.talos.runtime.scene.components.TransformComponent;

public class TransformComponentProvider extends AComponentProvider<TransformComponent> {

	public TransformComponentProvider (TransformComponent component) {
		super(component);
	}

	@Override
	public Array<PropertyWidget> getListOfProperties () {
		Array<PropertyWidget> properties = new Array<>();

		PropertyWidget positionWidget = WidgetFactory.generate(component, "position", "Position");
		PropertyWidget rotationWidget = WidgetFactory.generate(component, "rotation", "Rotation");
		PropertyWidget scaleWidget = WidgetFactory.generate(component, "scale", "Scale");

		properties.add(positionWidget);
		properties.add(rotationWidget);
		properties.add(scaleWidget);

		return properties;
	}

	@Override
	public String getPropertyBoxTitle () {
		return "Transform";
	}

	@Override
	public int getPriority () {
		return 1;
	}

}
