package com.talosvfx.talos.editor.addons.scene.events;

import com.talosvfx.talos.editor.addons.scene.utils.FileWatching;
import com.talosvfx.talos.editor.notifications.Notifications;

public class ProjectDirectoryContentsChanged implements Notifications.Event {

    private FileWatching.Changes changes;

    @Override
    public void reset() {

    }

    public Notifications.Event set(FileWatching.Changes changes) {
        this.changes = changes;
        return this;
    }

    public FileWatching.Changes getChanges() {
        return changes;
    }
}
