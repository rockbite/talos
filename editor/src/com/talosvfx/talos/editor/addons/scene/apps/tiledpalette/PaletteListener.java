package com.talosvfx.talos.editor.addons.scene.apps.tiledpalette;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.maps.StaticTile;

public class PaletteListener implements EventListener {

    public boolean handle(Event e) {
        if (!(e instanceof PaletteEvent)) return false;
        PaletteEvent event = (PaletteEvent) e;

        switch (event.getType()) {
            case selected:
                GameObject gameObject = null;
                if (event.getSelectedGameObjects() != null && event.getSelectedGameObjects().length > 0) {
                    gameObject = event.getSelectedGameObjects()[0];
                }
                StaticTile staticTile = null;
                if (gameObject == null &&  event.getSelectedTiles() != null && event.getSelectedTiles().length > 0) {
                    staticTile = event.getSelectedTiles()[0];
                }
                return selected(event, gameObject, staticTile); // handle selection
            case selectedMultiple:
                return selectedMultiple(event, event.getSelectedGameObjects(), event.getSelectedTiles()); // handle selection
            case imported:
                return imported(event);
            case removed:
                return removed(event);
            case moved:
                return moved(event);
            case lostFocus:
                return lostFocus(event);
            case startTranslate:
                startTranslate(event);
                return false;
            case startGizmoEdit:
                startGizmoEdit(event);
                return false;
        }
        return false;
    }

    public boolean selected(PaletteEvent e, GameObject gameObject, StaticTile tle) { // pass selected things
        return false;
    }

    public boolean selectedMultiple(PaletteEvent e, GameObject[] gameObjects, StaticTile[] staticTiles) { // pass selected things
        return false;
    }

    public boolean lostFocus(PaletteEvent e) { // game assets of selected items
        return false;
    }

    public void startTranslate(PaletteEvent e) { // game assets of selected items
        return;
    }

    public void startGizmoEdit(PaletteEvent e) { // game assets of selected items
        return;
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

