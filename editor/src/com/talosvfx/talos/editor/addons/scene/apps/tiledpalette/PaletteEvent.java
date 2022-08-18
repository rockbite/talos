package com.talosvfx.talos.editor.addons.scene.apps.tiledpalette;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;

public class PaletteEvent extends Event {
    private Type type;
    private Array<GameAsset<?>> selectedGameAssets;
    private PaletteEditor.PaletteImportMode currentMode;

    public void reset () {
        super.reset();
        selectedGameAssets = null;
        currentMode = PaletteEditor.PaletteImportMode.TILE_ENTITY;
    }

    public void setSelectedGameAssets(Array<GameAsset<?>> gameAssets) {
        this.selectedGameAssets = gameAssets;
    }

    public Array<GameAsset<?>> getSelectedGameAssets() {
        return selectedGameAssets;
    }

    public Type getType() {
        return type;
    }

    public void setType (Type type) {
        this.type = type;
    }

    public PaletteEditor.PaletteImportMode getCurrentFilterMode () {
        return currentMode;
    }

    public void setCurrentFilterMode (PaletteEditor.PaletteImportMode mode) {
        this.currentMode = mode;
    }

    static public enum Type {
        selected,
        selectedMultiple,
        imported,
        removed,
        moved,
        lostFocus,
    }
}
