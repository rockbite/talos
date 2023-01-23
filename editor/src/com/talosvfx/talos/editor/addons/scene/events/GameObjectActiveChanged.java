package com.talosvfx.talos.editor.addons.scene.events;

import com.talosvfx.talos.runtime.scene.GameObject;import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.TalosEvent;

public class GameObjectActiveChanged implements TalosEvent {

    public GameObject target;

    @Override
    public void reset () {
        target = null;
    }
}
