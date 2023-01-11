package com.talosvfx.talos.editor.addons.scene;


import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import lombok.Data;

@Data
public class SceneLayer {
    private String name;
    private int index;

    public SceneLayer(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public SceneLayer() {

    }
}
