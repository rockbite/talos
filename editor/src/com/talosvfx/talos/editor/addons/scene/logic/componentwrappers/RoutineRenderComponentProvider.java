package com.talosvfx.talos.editor.addons.scene.logic.componentwrappers;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.SceneUtils;
import com.talosvfx.talos.editor.addons.scene.widgets.property.PropertyPanelAssetSelectionWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.WidgetFactory;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.runtime.routine.serialization.BaseRoutineData;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.components.RendererComponent;
import com.talosvfx.talos.runtime.scene.components.RoutineRendererComponent;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyWrapper;

import java.util.function.Supplier;

public class RoutineRenderComponentProvider extends RendererComponentProvider<RoutineRendererComponent> {

	Array<PropertyWidget> properties = new Array<>();

	public RoutineRenderComponentProvider (RoutineRendererComponent component) {
		super(component);
	}

	@Override
	public Array<PropertyWidget> getListOfProperties () {
		properties.clear();
		PropertyWidget widget = WidgetFactory.generateForGameAsset(component, "routineResource", null, "Routine", GameAssetType.ROUTINE);

		properties.add(widget);

		PropertyWidget sizeWidget = WidgetFactory.generate(component, "viewportSize", "Viewport");
		properties.add(sizeWidget);

		PropertyWidget cacheWidget = WidgetFactory.generate(component, "cacheCoolDown", "Cache");
		properties.add(cacheWidget);

		Array<PropertyWidget> superList = super.getListOfProperties();
		properties.addAll(superList);

		final Array<PropertyWrapper<?>> propertyWrappers = component.propertyWrappers;
		for (PropertyWrapper<?> propertyWrapper : propertyWrappers) {
			PropertyWidget<?> generate = WidgetFactory.generateForPropertyWrapper(propertyWrapper);
			generate.setInjectedChangeListener(new ChangeListener() {
				@Override
				public void changed (ChangeEvent event, Actor actor) {
					propertyWrapper.isValueOverridden = true;
					component.routineInstance.setDirty();
				}
			});
			generate.setParent(this);
			properties.add(generate);
		}

		return properties;
	}

	@Override
	public String getPropertyBoxTitle () {
		return "Routine Renderer";
	}

	@Override
	public int getPriority () {
		return 4;
	}

}
