package com.talosvfx.talos.editor.addons.scene.events;

import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.notifications.Notifications;

public class GameObjectCreated implements Notifications.Event {

    private GameObject target;

    public GameObjectCreated setTarget(GameObject target) {
        this.target = target;

        return this;
    }

    public GameObject getTarget() {
        return target;
    }

    @Override
    public void reset () {
        target = null;
    }
}
