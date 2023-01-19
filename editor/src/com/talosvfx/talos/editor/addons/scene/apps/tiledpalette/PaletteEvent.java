package com.talosvfx.talos.editor.addons.scene.apps.tiledpalette;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.talosvfx.talos.runtime.scene.GameObject;import com.talosvfx.talos.runtime.maps.StaticTile;

public class PaletteEvent extends Event {
    private Type type;
    private GameObject[] selectedGameObjects;
    private StaticTile[] selectedTiles;

    public void reset () {
        super.reset();
        selectedTiles = null;
        selectedGameObjects = null;
    }

    public void setSelectedTiles(StaticTile[] gameAssets) {
        this.selectedTiles = gameAssets;
    }

    public void setSelectedGameObjects(GameObject[] gameObjects) {
        this.selectedGameObjects = gameObjects;
    }

    public GameObject[] getSelectedGameObjects() {
        return selectedGameObjects;
    }

    public StaticTile[] getSelectedTiles() {
        return selectedTiles;
    }

    public Type getType() {
        return type;
    }

    public void setType (Type type) {
        this.type = type;
    }

    public enum Type {
        startTranslate,
        startGizmoEdit,
        selected,
        selectedMultiple,
        imported,
        removed,
        moved,
        lostFocus,
    }
}
