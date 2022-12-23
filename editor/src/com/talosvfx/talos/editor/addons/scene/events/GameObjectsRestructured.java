package com.talosvfx.talos.editor.addons.scene.events;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.notifications.TalosEvent;

public class GameObjectsRestructured implements TalosEvent {
    public Array<GameObject> targets;

    public GameObjectsRestructured () {
        targets = new Array<>(false, 16);
    }

    @Override
    public void reset () {
        targets.clear();
    }
}
