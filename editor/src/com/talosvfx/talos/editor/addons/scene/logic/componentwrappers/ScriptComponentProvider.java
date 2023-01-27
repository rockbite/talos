package com.talosvfx.talos.editor.addons.scene.logic.componentwrappers;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.widgets.property.PropertyPanelAssetSelectionWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.WidgetFactory;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.runtime.scene.components.ScriptComponent;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyWrapper;

import java.util.function.Supplier;

public class ScriptComponentProvider extends AComponentProvider<ScriptComponent> {

	public ScriptComponentProvider (ScriptComponent component) {
		super(component);
	}

	@Override
	public Array<PropertyWidget> getListOfProperties () {
		Array<PropertyWidget> properties = new Array<>();

		PropertyPanelAssetSelectionWidget<String> widget = new PropertyPanelAssetSelectionWidget<>("Script", GameAssetType.SCRIPT, new Supplier<GameAsset<String>>() {
			@Override
			public GameAsset<String> get () {
				return component.getScriptResource();
			}
		}, new PropertyWidget.ValueChanged<GameAsset<String>>() {
			@Override
			public void report (GameAsset<String> value) {
				component.setGameAsset(value);
			}
		});

		properties.add(widget);

		for (PropertyWrapper<?> scriptProperty : component.getScriptProperties()) {
			PropertyWidget generate = WidgetFactory.generateForPropertyWrapper(scriptProperty);
			generate.setParent(this);
			properties.add(generate);
		}

		return properties;
	}

	@Override
	public String getPropertyBoxTitle () {
		return "Script Component";
	}

	@Override
	public int getPriority () {
		return 4;
	}
}
