package com.talosvfx.talos.editor.addons.scene.logic.componentwrappers;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.WidgetFactory;
import com.talosvfx.talos.runtime.scene.components.CameraComponent;

public class CameraComponentProvider extends ComponentPropertyProvider<CameraComponent> {

	public CameraComponentProvider (CameraComponent component) {
		super(component);
	}

	@Override
	public Array<PropertyWidget> getListOfProperties () {
		Array<PropertyWidget> properties = new Array<>();

		PropertyWidget colorWidget = WidgetFactory.generate(component, "backgroundColor", "Background Color");
		PropertyWidget zoomWidget = WidgetFactory.generate(component, "zoom", "Zoom");
		PropertyWidget sizeWidget = WidgetFactory.generate(component, "size", "Size");

		properties.add(colorWidget);
		properties.add(zoomWidget);
		properties.add(sizeWidget);

		return properties;
	}

	@Override
	public String getPropertyBoxTitle () {
		return "Camera";
	}

	@Override
	public int getPriority () {
		return 2;
	}

	@Override
	public Class<? extends IPropertyProvider> getType () {
		return getClass();
	}
}
