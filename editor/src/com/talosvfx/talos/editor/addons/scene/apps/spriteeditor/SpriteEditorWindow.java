package com.talosvfx.talos.editor.addons.scene.apps.spriteeditor;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.DelayedRemovalArray;
import com.talosvfx.talos.runtime.assets.GameAsset;

public class SpriteEditorWindow extends Table {
    protected SpriteEditor spriteEditor;
    protected GameAsset<Texture> gameAsset;

    public SpriteEditorWindow (SpriteEditor spriteEditor) {
        this.spriteEditor = spriteEditor;
    }

    public void setScrollFocus() {

    }

    public void updateForGameAsset (GameAsset<Texture> gameAsset) {
        this.gameAsset = gameAsset;
    }

    private Array<EventListener> storedListeners = new Array<>();
    public void restoreListeners () {
        for (EventListener storedListener : storedListeners) {
            addListener(storedListener);
        }
    }

    public void disableListeners () {
        DelayedRemovalArray<EventListener> listeners = getListeners();
        storedListeners.addAll(listeners);
        clearListeners();
    }
}
