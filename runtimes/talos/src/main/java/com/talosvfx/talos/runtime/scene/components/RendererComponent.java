package com.talosvfx.talos.runtime.scene.components;

import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.SceneLayer;


public abstract class RendererComponent extends AComponent implements Json.Serializable {

    public SceneLayer sortingLayer = SceneLayer.DEFAULT_SCENE_LAYER;
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
        json.writeValue("sortingSceneLayer", sortingLayer);
        json.writeValue("orderingInLayer", orderingInLayer);
        json.writeValue("visible", visible);
        json.writeValue("childrenVisible", childrenVisible);

    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        sortingLayer = json.readValue("sortingSceneLayer", SceneLayer.class, SceneLayer.DEFAULT_SCENE_LAYER, jsonData);
        orderingInLayer = jsonData.getInt("orderingInLayer", 0);
        visible = jsonData.getBoolean("visible", true);
        childrenVisible = jsonData.getBoolean("childrenVisible", true);
    }

    public abstract void minMaxBounds (GameObject parentEntity, BoundingBox rectangle);

    @Override
    public void reset() {
        super.reset();
        sortingLayer = SceneLayer.DEFAULT_SCENE_LAYER;
        orderingInLayer = 0;
        visible = true;
        childrenVisible = true;
    }
}
