package com.talosvfx.talos.runtime.scene;


import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.scene.render.RenderStrategy;
import lombok.Data;

import java.util.UUID;

@Data
public class SceneLayer implements Json.Serializable {
    private UUID uniqueID;
    private String name;
    private int index;

    private RenderStrategy renderStrategy = RenderStrategy.SCENE;

    public SceneLayer(String name, int index) {
        this.name = name;
        this.index = index;
        this.uniqueID = UUID.randomUUID();
    }

    public SceneLayer(String name, int index, UUID uniqueID) {
        this.name = name;
        this.index = index;
        this.uniqueID = uniqueID;
    }

    public SceneLayer() {
        uniqueID = UUID.randomUUID();
    }

    @Override
    public void write (Json json) {
        json.writeValue("name", name);
        json.writeValue("uuid", uniqueID.toString());
        json.writeValue("index", index);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        name = jsonData.getString("name");
        if (jsonData.has("uuid")) {
            uniqueID = UUID.fromString(jsonData.getString("uuid"));
        } else {
            uniqueID = UUID.randomUUID();
        }
        index = jsonData.getInt("index");
    }
}
