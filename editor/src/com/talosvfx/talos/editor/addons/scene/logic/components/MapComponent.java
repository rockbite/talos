package com.talosvfx.talos.editor.addons.scene.logic.components;

import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.addons.scene.MainRenderer;
import com.talosvfx.talos.editor.addons.scene.events.LayerListUpdated;
import com.talosvfx.talos.editor.addons.scene.events.TalosLayerSelectEvent;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.maps.MapType;
import com.talosvfx.talos.editor.addons.scene.maps.TalosLayer;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.widgets.propertyWidgets.DynamicItemListWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.TalosLayerPropertiesWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.WidgetFactory;
import com.talosvfx.talos.editor.widgets.ui.FilteredTree;

import java.util.function.Supplier;

public class MapComponent extends RendererComponent {


    private Array<TalosLayer> layers = new Array<>();
    private MapType mapType = MapType.ORTHOGRAPHIC_TOPDOWN;
    public transient TalosLayer selectedLayer;
    public transient MainRenderer mainRenderer = new MainRenderer();

    @Override
    public Array<PropertyWidget> getListOfProperties () {

        Array<PropertyWidget> properties = super.getListOfProperties();

        Supplier<TalosLayer> supplier = new Supplier<TalosLayer>() {
            @Override
            public TalosLayer get () {
                return new TalosLayer("NewLayer");
            }
        };
        DynamicItemListWidget<TalosLayer> itemListWidget = new DynamicItemListWidget<>("Layers", new Supplier<Array<TalosLayer>>() {
            @Override
            public Array<TalosLayer> get () {
                return layers;
            }
        }, new PropertyWidget.ValueChanged<Array<TalosLayer>>() {
            @Override
            public void report (Array<TalosLayer> value) {
                layers.clear();
                for (TalosLayer item : value) {
                    layers.add(item);
                }

                Notifications.fireEvent(Notifications.obtainEvent(LayerListUpdated.class));
            }
        }, new DynamicItemListWidget.DynamicItemListInteraction<TalosLayer>() {
            @Override
            public Supplier<TalosLayer> newInstanceCreator () {

                return supplier;
            }

            @Override
            public String getID (TalosLayer o) {
                return o.getName();
            }

            @Override
            public void updateName (TalosLayer talosLayer, String newText) {
                talosLayer.setName(newText);
            }
        });

        TalosLayerPropertiesWidget talosLayerPropertiesWidget = new TalosLayerPropertiesWidget(null, new Supplier<TalosLayer>() {
            @Override
            public TalosLayer get () {
                return selectedLayer;
            }
        }, new PropertyWidget.ValueChanged<TalosLayer>() {
            @Override
            public void report (TalosLayer value) {
            }
        });
        itemListWidget.list.addItemListener(new FilteredTree.ItemListener<TalosLayer>() {
            @Override
            public void chosen (FilteredTree.Node<TalosLayer> node) {
                super.chosen(node);
                selectedLayer = node.getObject();
                TalosLayerSelectEvent talosLayerSelectEvent = Notifications.obtainEvent(TalosLayerSelectEvent.class);
                talosLayerSelectEvent.layer = selectedLayer;
                Notifications.fireEvent(talosLayerSelectEvent);
                talosLayerPropertiesWidget.updateWidget(node.getObject());
                talosLayerPropertiesWidget.toggleHide(false);
            }
        });

        properties.add(WidgetFactory.generate(this, "mapType", "Type"));
        properties.add(itemListWidget);
        properties.add(talosLayerPropertiesWidget);


        return properties;
    }

    @Override
    public void minMaxBounds (GameObject parentEntity, BoundingBox rectangle) {
        //todo
    }

    @Override
    public void write (Json json) {
        super.write(json);

        json.writeValue("layers", layers);
        json.writeValue("mapType", mapType);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        layers = json.readValue(Array.class, TalosLayer.class, jsonData.get("layers"));
        mapType = json.readValue(MapType.class, jsonData.get("mapType"));
    }

    @Override
    public String getPropertyBoxTitle () {
        return "Map";
    }

    @Override
    public int getPriority () {
        return 2;
    }

    @Override
    public Class<? extends IPropertyProvider> getType() {
        return getClass();
    }

    public Array<TalosLayer> getLayers () {
        return layers;
    }

    public MapType getMapType () {
        return mapType;
    }
}
