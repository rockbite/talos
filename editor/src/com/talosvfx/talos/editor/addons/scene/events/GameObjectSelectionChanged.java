package com.talosvfx.talos.editor.addons.scene.events;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.TalosEvent;
import com.talosvfx.talos.editor.widgets.ui.ViewportWidget;

public class GameObjectSelectionChanged implements TalosEvent {

    private ObjectSet<GameObject> objectArray = new ObjectSet<>();
    private ViewportWidget context;

    public GameObjectSelectionChanged set(ViewportWidget context, ObjectSet<GameObject> arr) {
        objectArray.clear();
        objectArray.addAll(arr);

        this.context = context;

        return this;
    }

    public ViewportWidget getContext () {
        return context;
    }

    public ObjectSet<GameObject> get() {
        return objectArray;
    }

    @Override
    public void reset () {
        objectArray.clear();
    }
}
