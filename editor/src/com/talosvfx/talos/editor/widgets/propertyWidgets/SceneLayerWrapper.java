package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.talosvfx.talos.runtime.scene.SceneLayer;
import lombok.Getter;

import java.util.UUID;

public class SceneLayerWrapper {
    @Getter
    private SceneLayer instance;

    public SceneLayerWrapper(SceneLayer sceneLayer) {
        instance = sceneLayer;
    }

    @Override
    public String toString () {
        return instance.getName();
    }

    public void updateName (String newText) {
        instance.setName(newText);
    }

    public boolean canDelete() {
        if (instance.getName().equals("Default")) {
            return false;
        }
        return true;
    }

    public UUID getID() {
        return instance.getUniqueID();
    }

    public String getName() {
        return instance.getName();
    }
}
