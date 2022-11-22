package com.talosvfx.talos.editor.addons.scene.events;

import com.talosvfx.talos.editor.addons.scene.logic.IPropertyHolder;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.TalosEvent;

public class PropertyHolderEdited implements TalosEvent {

    public Object parentOfPropertyHolder;
    public boolean fastChange = false;

    @Override
    public void reset () {
        fastChange = false;
        parentOfPropertyHolder = null;
    }
}
