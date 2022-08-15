package com.talosvfx.talos.editor.addons.scene.events;

import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.notifications.Notifications;

public class GameObjectActiveChanged implements Notifications.Event {

    public GameObject target;

    @Override
    public void reset () {
        target = null;
    }
}
