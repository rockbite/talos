package com.talosvfx.talos.editor.addons.scene.events;

import com.talosvfx.talos.editor.notifications.TalosEvent;

public class AssetPathChanged implements TalosEvent {

    public String oldRelativePath;
    public String newRelativePath;

    @Override
    public void reset () {

    }
}
