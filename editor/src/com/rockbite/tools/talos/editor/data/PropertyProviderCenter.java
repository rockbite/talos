package com.rockbite.tools.talos.editor.data;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;
import com.rockbite.tools.talos.editor.widgets.propertyWidgets.CheckboxWidget;
import com.rockbite.tools.talos.editor.widgets.propertyWidgets.FloatWidget;
import com.rockbite.tools.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.rockbite.tools.talos.editor.widgets.propertyWidgets.LabelWidget;
import com.rockbite.tools.talos.editor.wrappers.Property;

public class PropertyProviderCenter {

	private ObjectMap<Class, Pool<? extends PropertyWidget>> propertyWidgetMap = new ObjectMap<>();

	private static PropertyProviderCenter instance;

	public static PropertyProviderCenter Instance () {
		if (instance == null) {
			instance = new PropertyProviderCenter();
			instance.registerMaps();
		}

		return instance;
	}

	public PropertyWidget obtainWidgetForProperty (Property property) {
		Pool<? extends PropertyWidget> pool = propertyWidgetMap.get(property.getValueClass());
		PropertyWidget obtain = pool.obtain();
		obtain.configureForProperty(property);

		return obtain;
	}

	private void registerMaps () {
		Pool<LabelWidget> stringWidgetPool = new Pool<LabelWidget>() {
			@Override
			protected LabelWidget newObject () {
				return new LabelWidget();
			}
		};
		Pool<FloatWidget> floatWidgetPool = new Pool<FloatWidget>() {
			@Override
			protected FloatWidget newObject () {
				return new FloatWidget();
			}
		};
		Pool<CheckboxWidget> checkboxWidgetPool = new Pool<CheckboxWidget>() {
			@Override
			protected CheckboxWidget newObject () {
				return new CheckboxWidget();
			}
		};

		propertyWidgetMap.put(String.class, stringWidgetPool);
		propertyWidgetMap.put(Float.class, floatWidgetPool);
		propertyWidgetMap.put(Boolean.class, checkboxWidgetPool);
	}
}
