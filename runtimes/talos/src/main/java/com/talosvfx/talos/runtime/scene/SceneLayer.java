package com.talosvfx.talos.runtime.scene;


import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import lombok.Data;

@Data
public class SceneLayer {

    public static SceneLayer DEFAULT_SCENE_LAYER = new SceneLayer("Default", 0);

    private String name;
    private int index;

    public SceneLayer(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public SceneLayer() {

    }
}
