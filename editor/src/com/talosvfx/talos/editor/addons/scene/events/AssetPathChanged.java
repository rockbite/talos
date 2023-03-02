package com.talosvfx.talos.editor.addons.scene.events;

import com.badlogic.gdx.files.FileHandle;
import com.talosvfx.talos.editor.notifications.TalosEvent;

public class AssetPathChanged implements TalosEvent {

    public FileHandle oldHandle;
    public FileHandle newHandle;

    @Override
    public void reset () {

    }
}
