package com.talosvfx.talos.editor.addons.scene.events;

import com.talosvfx.talos.editor.addons.scene.utils.FileWatching;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.TalosEvent;

public class ProjectDirectoryContentsChanged implements TalosEvent {

    private FileWatching.Changes changes;

    @Override
    public void reset() {

    }

    public ProjectDirectoryContentsChanged set(FileWatching.Changes changes) {
        this.changes = changes;
        return this;
    }

    public FileWatching.Changes getChanges() {
        return changes;
    }
}
