package com.talosvfx.talos.editor.addons.scene.events;

import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineInstance;
import com.talosvfx.talos.editor.notifications.TalosEvent;

public class RoutineUpdated implements TalosEvent {

    public RoutineInstance routineInstance;

    public RoutineUpdated set(RoutineInstance routineInstance) {
        this.routineInstance = routineInstance;
        return this;
    }

    @Override
    public void reset () {
        routineInstance = null;
    }
}
