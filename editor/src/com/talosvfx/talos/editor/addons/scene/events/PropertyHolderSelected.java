package com.talosvfx.talos.editor.addons.scene.events;

import com.talosvfx.talos.editor.addons.scene.logic.IPropertyHolder;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.TalosEvent;

public class PropertyHolderSelected<T extends IPropertyHolder> implements TalosEvent {

    private T target;

    public PropertyHolderSelected setTarget(T target) {
        this.target = target;

        return this;
    }

    public T getTarget() {
        return target;
    }

    @Override
    public void reset () {
        target = null;
    }
}
