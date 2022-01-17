package com.talosvfx.talos.editor.addons.scene.events;

import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.notifications.Notifications;

public class GameObjectNameChanged implements Notifications.Event {

    public GameObject target;
    public String oldName;
    public String newName;

    @Override
    public void reset () {
        target = null;
    }
}
