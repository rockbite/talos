package com.talosvfx.talos.editor.addons.scene.events;

import com.badlogic.gdx.utils.ObjectSet;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.editor.notifications.ContextRequiredEvent;

public class GameObjectSelectionChanged<T> extends ContextRequiredEvent<T> {

    private ObjectSet<GameObject> objectArray = new ObjectSet<>();

    public GameObjectSelectionChanged set(T context, ObjectSet<GameObject> arr) {
        setContext(context);

        objectArray.clear();
        objectArray.addAll(arr);

        return this;
    }

    public ObjectSet<GameObject> get() {
        return objectArray;
    }

    @Override
    public void reset () {
        objectArray.clear();
    }
}
