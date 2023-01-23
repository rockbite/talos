package com.talosvfx.talos.editor.addons.scene.logic.componentwrappers;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.WidgetFactory;
import com.talosvfx.talos.runtime.maps.GridPosition;
import com.talosvfx.talos.runtime.scene.components.TileDataComponent;

public class TileDataComponentProvider extends AComponentProvider<TileDataComponent> {

	public TileDataComponentProvider (TileDataComponent component) {
		super(component);
	}

	@Override
	public Array<PropertyWidget> getListOfProperties () {
		Array<PropertyWidget> properties = new Array<>();

		properties.add(WidgetFactory.generate(component, "fakeZ", "FakeZ"));
		properties.add(WidgetFactory.generate(component, "visualOffset", "VisualOffset"));

		return properties;
	}

	public ObjectSet<GridPosition> getParentTiles () {
		return component.parentTiles;
	}

	public void setParentTiles (ObjectSet<GridPosition> parentTiles) {
		component.parentTiles = parentTiles;
	}

	@Override
	public String getPropertyBoxTitle () {
		return "TileData Component";
	}

	@Override
	public int getPriority () {
		return 4;
	}

}
