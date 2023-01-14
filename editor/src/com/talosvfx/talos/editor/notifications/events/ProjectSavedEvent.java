package com.talosvfx.talos.editor.notifications.events;

import com.talosvfx.talos.editor.notifications.TalosEvent;

public class ProjectSavedEvent implements TalosEvent {

    String projectName;

    public ProjectSavedEvent set(String projectName) {
        this.projectName = projectName;

        return this;
    }

    @Override
    public void reset () {
        projectName = null;
    }
}
