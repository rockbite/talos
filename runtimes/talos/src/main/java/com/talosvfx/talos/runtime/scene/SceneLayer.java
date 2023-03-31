package com.talosvfx.talos.runtime.scene;


import com.talosvfx.talos.runtime.scene.render.RenderStrategy;
import lombok.Data;

import java.util.UUID;

@Data
public class SceneLayer {
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
}
