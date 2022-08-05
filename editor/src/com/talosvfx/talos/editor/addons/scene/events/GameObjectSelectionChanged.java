package com.talosvfx.talos.editor.addons.scene.events;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.widgets.ui.ViewportWidget;

public class GameObjectSelectionChanged implements Notifications.Event {

    private Array<GameObject> objectArray = new Array<>();
    private ViewportWidget context;

    public GameObjectSelectionChanged set(ViewportWidget context, Array<GameObject> arr) {
        objectArray.clear();
        objectArray.addAll(arr);

        this.context = context;

        return this;
    }

    public ViewportWidget getContext () {
        return context;
    }

    public Array<GameObject> get() {
        return objectArray;
    }

    @Override
    public void reset () {
        objectArray.clear();
    }
}
