package com.talosvfx.talos.editor.addons.scene.events;

import com.talosvfx.talos.editor.addons.scene.logic.components.AComponent;
import com.talosvfx.talos.editor.notifications.Notifications;

public class ComponentUpdated implements Notifications.Event {

    private AComponent component;
    private boolean rapid;

    @Override
    public void reset () {
        component = null;
        rapid = false;
    }

    public Notifications.Event set (AComponent component, boolean rapid) {
        this.rapid = rapid;
        this.component = component;
        return this;
    }

    public Notifications.Event set (AComponent component) {
        return set(component, false);
    }

    public AComponent getComponent () {
        return component;
    }

    public boolean wasRapid () {
        return rapid;
    }
}
