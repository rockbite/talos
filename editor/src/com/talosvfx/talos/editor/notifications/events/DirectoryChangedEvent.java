package com.talosvfx.talos.editor.notifications.events;

import com.talosvfx.talos.editor.notifications.TalosEvent;
import lombok.Getter;

public class DirectoryChangedEvent implements TalosEvent {

    @Getter
    private String directoryPath;

    @Override
    public void reset() {
        directoryPath = null;
    }

    public DirectoryChangedEvent set(String path) {
        directoryPath = path;

        return this;
    }
}
