package com.talosvfx.talos.editor.addons.scene.events;

import com.badlogic.gdx.utils.ObjectSet;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.notifications.ContextRequiredEvent;

public class GameObjectSelectionChanged extends ContextRequiredEvent {

    private ObjectSet<GameObject> objectArray = new ObjectSet<>();

    public GameObjectSelectionChanged set(Context context, ObjectSet<GameObject> arr) {
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
