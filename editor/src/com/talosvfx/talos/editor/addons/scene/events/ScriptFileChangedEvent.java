package com.talosvfx.talos.editor.addons.scene.events;

import com.badlogic.gdx.files.FileHandle;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.TalosEvent;

import java.nio.file.WatchEvent;

public class ScriptFileChangedEvent implements TalosEvent {

    public WatchEvent.Kind<?> eventType;
    public FileHandle file;

    @Override
    public void reset () {
        eventType = null;
        file = null;
    }

    public ScriptFileChangedEvent set (WatchEvent.Kind<?> eventType, FileHandle file) {
        this.eventType = eventType;
        this.file = file;
        return this;
    }
}
