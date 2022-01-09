package com.talosvfx.talos.editor.addons.scene.events;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.notifications.Notifications;



public class GameObjectSelectionChanged implements Notifications.Event {

    private Array<GameObject> objectArray = new Array<>();

    public GameObjectSelectionChanged set(Array<GameObject> arr) {
        objectArray.clear();
        objectArray.addAll(arr);

        return this;
    }

    public Array<GameObject> get() {
        return objectArray;
    }

    @Override
    public void reset () {
        objectArray.clear();
    }
}
