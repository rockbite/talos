package com.talosvfx.talos.editor.addons.scene.events.explorer;

import com.badlogic.gdx.files.FileHandle;
import com.talosvfx.talos.editor.notifications.TalosEvent;
import lombok.Getter;

public class DirectoryMovedEvent implements TalosEvent {

    @Getter
    private FileHandle oldHandle;
    @Getter
    private FileHandle newHandle;

    public DirectoryMovedEvent set (FileHandle oldHandle, FileHandle newHandle) {
        this.oldHandle = oldHandle;
        this.newHandle = newHandle;
        return this;
    }

    @Override
    public void reset() {
        oldHandle = null;
        newHandle = null;
    }
}
