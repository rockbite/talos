package com.talosvfx.talos.editor.addons.scene.logic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.*;
import com.talosvfx.talos.editor.addons.scene.events.LayerListUpdated;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.project2.TalosProjectData;
import com.talosvfx.talos.editor.project2.projectdata.SceneData;
import com.talosvfx.talos.editor.widgets.propertyWidgets.*;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.function.Supplier;

public class Scene extends SavableContainer implements IPropertyProvider {

    private static final Logger logger = LoggerFactory.getLogger(Scene.class);

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

    private String getNextAvailableLayerName (String base) {
        String newLayer = base;
        int count = 1;
        SceneData sceneData = SharedResources.currentProject.getSceneData();
        Array<String> renderLayers = sceneData.getRenderLayers();
        while (renderLayers.contains(newLayer, false)) {
            newLayer = base + count++;
        }

        return newLayer;
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

        Supplier<ItemData> newItemDataSupplier = new Supplier<ItemData>() {
            @Override
            public ItemData get () {
                String base = "NewLayer";
                String newLayer = getNextAvailableLayerName(base);

                return new ItemData(newLayer, newLayer);
            }
        };
        DynamicItemListWidget<ItemData> itemListWidget = new DynamicItemListWidget<ItemData>("Layers" , new Supplier<Array<ItemData>>() {
            @Override
            public Array<ItemData> get () {
                Array<ItemData> list = new Array<>();
                TalosProjectData currentProject = SharedResources.currentProject;
                Array<String> renderLayers = currentProject.getSceneData().getRenderLayers();
                for (String layerName : renderLayers) {
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
                TalosProjectData currentProject = SharedResources.currentProject;
                Array<String> renderLayers = currentProject.getSceneData().getRenderLayers();
                renderLayers.clear();
                for (ItemData item : value) {
                    renderLayers.add(item.text);
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

            @Override
            public String updateName (ItemData itemData, String newText) {
                newText = getNextAvailableLayerName(newText);
                itemData.updateName(newText);
                return newText;
            }
        });

        itemListWidget.setDraggableInLayerOnly(true);

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
