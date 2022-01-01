package com.talosvfx.talos.editor.addons.scene.logic.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.SelectBoxWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.WidgetFactory;

public abstract class RendererComponent implements Json.Serializable, IComponent {

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

        SelectBoxWidget layerWidget = new SelectBoxWidget("Sorting Layer") {
            @Override
            public Array<String> getOptionsList () {
                return SceneEditorAddon.get().workspace.getLayerList();
            }

            @Override
            public String getValue () {
                return sortingLayer;
            }

            @Override
            public void valueChanged (String value) {
                sortingLayer = value;
            }
        };

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
