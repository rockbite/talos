package com.talosvfx.talos.editor.addons.scene.logic.components;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.events.LayerListUpdated;
import com.talosvfx.talos.editor.addons.scene.maps.TalosLayer;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.widgets.propertyWidgets.DynamicItemListWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.TalosLayerPropertiesWidget;
import com.talosvfx.talos.editor.widgets.ui.FilteredTree;

import java.util.function.Supplier;

public class MapComponent extends AComponent {


    private Array<TalosLayer> layers = new Array<>();
    private transient TalosLayer selectedLayer;

    @Override
    public Array<PropertyWidget> getListOfProperties () {
        Array<PropertyWidget> properties = new Array<>();


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
                talosLayerPropertiesWidget.updateWidget(node.getObject());
                talosLayerPropertiesWidget.toggleHide(false);
            }
        });

        properties.add(itemListWidget);
        properties.add(talosLayerPropertiesWidget);


        return properties;
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
}
