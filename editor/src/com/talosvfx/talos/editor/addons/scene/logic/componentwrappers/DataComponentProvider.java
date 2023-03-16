package com.talosvfx.talos.editor.addons.scene.logic.componentwrappers;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.SceneUtils;
import com.talosvfx.talos.editor.widgets.propertyWidgets.ButtonPropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.WidgetFactory;
import com.talosvfx.talos.runtime.scene.components.DataComponent;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.DataPropertiesUtil;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyWrapper;

public class DataComponentProvider extends AComponentProvider<DataComponent> {

	public DataComponentProvider(DataComponent component) {
		super(component);
	}

	@Override
	public Array<PropertyWidget> getListOfProperties () {
		Array<PropertyWidget> properties = new Array<>();

		for (PropertyWrapper<?> property : component.getProperties()) {
			PropertyWidget generate = WidgetFactory.generateForPropertyWrapper(property);
			generate.setParent(component);
			properties.add(generate);
		}

		ButtonPropertyWidget buttonPropertyWidget = new ButtonPropertyWidget("Add Field", widget -> {
				DataPropertiesUtil.addWrapper(component, "float", "something");
				SceneUtils.componentUpdated(component.getGameObject().getGameObjectContainerRoot(), component.getGameObject(), component, false);
		});
		properties.add(buttonPropertyWidget);
		return properties;
	}

	@Override
	public String getPropertyBoxTitle () {
		return "Data Component";
	}

	@Override
	public int getPriority () {
		return 4;
	}
}
