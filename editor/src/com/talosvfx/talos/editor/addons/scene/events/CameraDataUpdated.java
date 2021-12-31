package com.talosvfx.talos.editor.addons.scene.events;

import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.notifications.Notifications;

public class CameraDataUpdated implements Notifications.Event {

    public GameObject gameObject;

    @Override
    public void reset () {
        gameObject = null;
    }

    public Notifications.Event set (GameObject gameObject) {
        this.gameObject = gameObject;

        return this;
    }
}
