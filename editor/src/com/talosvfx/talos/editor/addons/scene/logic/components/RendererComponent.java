package com.talosvfx.talos.editor.addons.scene.logic.components;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.SelectBoxWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.WidgetFactory;

import java.util.function.Supplier;

public abstract class RendererComponent extends AComponent implements Json.Serializable {

    public String sortingLayer = "Default";
    public int orderingInLayer;

    public String getSortingLayer () {
        return sortingLayer;
    }

    public void setSortingLayer (String name) {
        sortingLayer = name;
    }

    @Override
    public Array<PropertyWidget> getListOfProperties () {
        Array<PropertyWidget> properties = new Array<>();

        PropertyWidget orderingInLayerWidget = WidgetFactory.generate(this, "orderingInLayer", "Ordering");

        SelectBoxWidget layerWidget = new SelectBoxWidget("Sorting Layer", new Supplier<String>() {
            @Override
            public String get() {
                return sortingLayer;
            }
        }, new PropertyWidget.ValueChanged<String>() {
            @Override
            public void report(String value) {
                sortingLayer = value;
            }
        }, new Supplier<Array<String>>() {
            @Override
            public Array<String> get() {
                return SceneEditorAddon.get().workspace.getLayerList();
            }
        });

        properties.add(orderingInLayerWidget);
        properties.add(layerWidget);

        return properties;
    }

    @Override
    public void write (Json json) {
        json.writeValue("sortingLayer", sortingLayer);
        json.writeValue("orderingInLayer", orderingInLayer);

    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        sortingLayer = jsonData.getString("sortingLayer", "Default");
        orderingInLayer = jsonData.getInt("orderingInLayer", 0);
    }
}
