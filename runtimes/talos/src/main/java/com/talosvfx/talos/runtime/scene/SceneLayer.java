package com.talosvfx.talos.runtime.scene;


import com.talosvfx.talos.runtime.scene.render.RenderStrategy;
import lombok.Data;

@Data
public class SceneLayer {

    public static SceneLayer DEFAULT_SCENE_LAYER = new SceneLayer("Default", 0);

    private String name;
    private int index;

    private RenderStrategy renderStrategy = RenderStrategy.SCENE;

    public SceneLayer(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public SceneLayer() {

    }
}
