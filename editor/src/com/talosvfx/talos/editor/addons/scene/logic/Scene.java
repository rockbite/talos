package com.talosvfx.talos.editor.addons.scene.logic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.*;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.events.LayerListUpdated;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.widgets.propertyWidgets.*;

import java.util.function.Supplier;

public class Scene extends SavableContainer implements IPropertyProvider {

    public Scene() {
        super();
    }

    public Scene(String path) {
        super(path);
    }

    @Override
    public Class<? extends IPropertyProvider> getType() {
        return getClass();
    }

    @Override
    public String getName () {
        FileHandle fileHandle = Gdx.files.absolute(path);
        return fileHandle.nameWithoutExtension();
    }

    @Override
    public void setName (String name) {
        root.setName(name);
    }

    @Override
    public Iterable<IPropertyProvider> getPropertyProviders () {
        Array<IPropertyProvider> list = new Array<>();

        list.add(this);

        return list;
    }

    @Override
    public Array<PropertyWidget> getListOfProperties () {
        Array<PropertyWidget> properties = new Array<>();

        LabelWidget labelWidget = new LabelWidget("Name", new Supplier<String>() {
            @Override
            public String get() {
                FileHandle file = Gdx.files.absolute(path);
                String name = file.nameWithoutExtension();
                return name;
            }
        });

        final SceneEditorWorkspace workspace = SceneEditorAddon.get().workspace;

        Supplier<ItemData> newItemDataSupplier = new Supplier<ItemData>() {
            @Override
            public ItemData get () {
                return new ItemData("NewLayer", "NewLayer");
            }
        };
        DynamicItemListWidget<ItemData> itemListWidget = new DynamicItemListWidget<ItemData>("Layers", new Supplier<Array<ItemData>>() {
            @Override
            public Array<ItemData> get () {
                Array<ItemData> list = new Array<>();
                for (String layerName : workspace.layers) {
                    ItemData itemData = new ItemData(layerName);
                    if (layerName.equals("Default")) {
                        itemData.canDelete = false;
                    }
                    list.add(itemData);
                }
                return list;
            }
        }, new PropertyWidget.ValueChanged<Array<ItemData>>() {
            @Override
            public void report (Array<ItemData> value) {
                workspace.layers.clear();
                for (ItemData item : value) {
                    workspace.layers.add(item.text);
                }

                Notifications.fireEvent(Notifications.obtainEvent(LayerListUpdated.class));
            }
        }, new DynamicItemListWidget.DynamicItemListInteraction<ItemData>() {
            @Override
            public Supplier<ItemData> newInstanceCreator () {
                return newItemDataSupplier;
            }

            @Override
            public String getID (ItemData o) {
                return o.id;
            }
        });

        properties.add(labelWidget);
        properties.add(itemListWidget);

        return properties;
    }

    @Override
    protected void writeData (Json json) {
        json.writeValue("name", getName());
        super.writeData(json);
    }

    @Override
    public String getPropertyBoxTitle () {
        return "Scene Properties";
    }

    @Override
    public int getPriority () {
        return 0;
    }
}
