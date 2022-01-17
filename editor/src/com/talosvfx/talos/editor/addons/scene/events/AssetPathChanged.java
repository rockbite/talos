package com.talosvfx.talos.editor.addons.scene.events;

import com.talosvfx.talos.editor.notifications.Notifications;

public class AssetPathChanged implements Notifications.Event{

    public String oldRelativePath;
    public String newRelativePath;

    @Override
    public void reset () {

    }
}
