package com.talosvfx.talos.editor.addons.scene.events;

import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.TalosEvent;

public class GameObjectNameChanged implements TalosEvent {

    public GameObject target;
    public String oldName;
    public String newName;

    @Override
    public void reset () {
        target = null;
    }
}
