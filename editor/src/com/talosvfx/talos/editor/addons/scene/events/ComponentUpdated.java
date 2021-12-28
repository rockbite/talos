package com.talosvfx.talos.editor.addons.scene.events;

import com.talosvfx.talos.editor.addons.scene.logic.components.IComponent;
import com.talosvfx.talos.editor.notifications.Notifications;

public class ComponentUpdated implements Notifications.Event {

    private IComponent component;

    @Override
    public void reset () {
        component = null;
    }

    public Notifications.Event set (IComponent component) {
        this.component = component;

        return this;
    }

    public IComponent getComponent () {
        return component;
    }
}
