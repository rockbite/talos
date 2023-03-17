package com.talosvfx.talos.runtime.scene.components;

import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.RuntimeContext;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.SceneData;
import com.talosvfx.talos.runtime.scene.SceneLayer;


public abstract class RendererComponent extends AComponent implements Json.Serializable {

    public SceneLayer sortingLayer;
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
    public void write (Json json) {
        json.writeValue("sortingSceneLayer", (sortingLayer != null) ? sortingLayer : RuntimeContext.getInstance().sceneData.getPreferredSceneLayer());
        json.writeValue("orderingInLayer", orderingInLayer);
        json.writeValue("visible", visible);
        json.writeValue("childrenVisible", childrenVisible);

    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        sortingLayer = json.readValue("sortingSceneLayer", SceneLayer.class, jsonData);
        if (RuntimeContext.getInstance().sceneData != null) {
            sortingLayer = RuntimeContext.getInstance().sceneData.getSceneLayerByName(sortingLayer.getName()); //connect it
        }
        orderingInLayer = jsonData.getInt("orderingInLayer", 0);
        visible = jsonData.getBoolean("visible", true);
        childrenVisible = jsonData.getBoolean("childrenVisible", true);
    }

    public abstract void minMaxBounds (GameObject parentEntity, BoundingBox rectangle);

    @Override
    public void reset() {
        super.reset();
        sortingLayer = RuntimeContext.getInstance().sceneData.getPreferredSceneLayer();
        orderingInLayer = 0;
        visible = true;
        childrenVisible = true;
    }
}
