package com.talosvfx.talos.editor.addons.scene.apps.tiledpalette;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;

public class PaletteListener implements EventListener {

    public boolean handle(Event e) {
        if (!(e instanceof PaletteEvent)) return false;
        PaletteEvent event = (PaletteEvent) e;

        switch (event.getType()) {
            case selected:
                return selected(event, event.getSelectedGameAssets().get(0), event.getCurrentFilterMode()); // handle selection
            case selectedMultiple:
                return selectedMultiple(event, event.getSelectedGameAssets().toArray(), event.getCurrentFilterMode()); // handle selection
            case imported:
                return imported(event); // handle importing
            case removed:
                return removed(event); // handle removal
            case moved:
                return moved(event); // handle movement
            case lostFocus:
                return lostFocus(event); // handle movement
        }
        return false;
    }

    public boolean selected(PaletteEvent e, GameAsset<?> gameAsset, PaletteEditor.PaletteFilterMode mode) { // game asset of the selected item
        return false;
    }

    public boolean selectedMultiple(PaletteEvent e, GameAsset<?>[] gameAssets, PaletteEditor.PaletteFilterMode mode) { // game assets of selected items
        return false;
    }

    public boolean lostFocus(PaletteEvent e) { // game assets of selected items
        return false;
    }

    public boolean imported(PaletteEvent e) {
        return false;
    }

    public boolean removed(PaletteEvent e) {
        return false;
    }

    public boolean moved(PaletteEvent e) {
        return false;
    }
}

