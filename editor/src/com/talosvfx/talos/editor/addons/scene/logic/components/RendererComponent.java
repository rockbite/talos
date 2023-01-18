package com.talosvfx.talos.editor.addons.scene.logic.components;

import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.addons.scene.MainRenderer;
import com.talosvfx.talos.editor.addons.scene.SceneLayer;
import com.talosvfx.talos.editor.addons.scene.SceneUtils;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.GameObjectContainer;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.project2.projectdata.SceneData;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.SelectBoxWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.WidgetFactory;

import java.util.function.Supplier;

public abstract class RendererComponent extends AComponent implements Json.Serializable {

    public SceneLayer sortingLayer = MainRenderer.DEFAULT_SCENE_LAYER;
    public int orderingInLayer;

    public boolean visible = true;
    public boolean childrenVisible = true;

    public SceneLayer getSortingLayer () {
        return sortingLayer;
    }

    public void setSortingLayer (SceneLayer name) {
        sortingLayer = name;
    }

    @Override
    public Array<PropertyWidget> getListOfProperties () {
        Array<PropertyWidget> properties = new Array<>();

        PropertyWidget visibleWidget = WidgetFactory.generate(this, "visible", "Visible");
        PropertyWidget childrenVisibleWidget = WidgetFactory.generate(this, "childrenVisible", "Children Visible");
        PropertyWidget orderingInLayerWidget = WidgetFactory.generate(this, "orderingInLayer", "Ordering");

        SelectBoxWidget layerWidget = new SelectBoxWidget("Sorting Layer", new Supplier<String>() {
            @Override
            public String get() {
                return sortingLayer.getName();
            }
        }, new PropertyWidget.ValueChanged<String>() {
            @Override
            public void report(String value) {
                RendererComponent rendererComponent = RendererComponent.this;
                rendererComponent.sortingLayer = SharedResources.currentProject.getSceneData().getSceneLayerByName(value);
                GameObject gameObject = rendererComponent.getGameObject();
                SceneUtils.componentUpdated(gameObject.getGameObjectContainerRoot(), gameObject, rendererComponent, false);
            }
        }, new Supplier<Array<String>>() {
            @Override
            public Array<String> get() {
                Array<String> layerNames = new Array<>();
                SceneData sceneData = SharedResources.currentProject.getSceneData();
                Array<SceneLayer> renderLayers = sceneData.getRenderLayers();
                for (SceneLayer renderLayer : renderLayers) {
                    layerNames.add(renderLayer.getName());
                }
                return layerNames;
            }
        });

        properties.add(visibleWidget);
        properties.add(childrenVisibleWidget);
        properties.add(orderingInLayerWidget);
        properties.add(layerWidget);

        return properties;
    }

    @Override
    public void write (Json json) {
        json.writeValue("sortingSceneLayer", sortingLayer);
        json.writeValue("orderingInLayer", orderingInLayer);
        json.writeValue("visible", visible);
        json.writeValue("childrenVisible", childrenVisible);

    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        sortingLayer = json.readValue("sortingSceneLayer", SceneLayer.class, MainRenderer.DEFAULT_SCENE_LAYER, jsonData);
        orderingInLayer = jsonData.getInt("orderingInLayer", 0);
        visible = jsonData.getBoolean("visible", true);
        childrenVisible = jsonData.getBoolean("childrenVisible", true);
    }

    public abstract void minMaxBounds (GameObject parentEntity, BoundingBox rectangle);

    @Override
    public void reset() {
        super.reset();
        sortingLayer = MainRenderer.DEFAULT_SCENE_LAYER;
        orderingInLayer = 0;
        visible = true;
        childrenVisible = true;
    }
}
