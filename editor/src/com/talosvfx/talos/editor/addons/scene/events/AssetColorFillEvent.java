package com.talosvfx.talos.editor.addons.scene.events;

import com.badlogic.gdx.files.FileHandle;
import com.talosvfx.talos.editor.notifications.TalosEvent;
import lombok.Getter;
import lombok.Setter;

public class AssetColorFillEvent implements TalosEvent {
    @Getter @Setter
    private FileHandle fileHandle;

    @Override
    public void reset() {
        fileHandle = null;
    }
}
