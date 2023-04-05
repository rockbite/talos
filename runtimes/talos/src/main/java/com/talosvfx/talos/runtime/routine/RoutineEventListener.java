package com.talosvfx.talos.runtime.routine;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyWrapper;

public interface RoutineEventListener {
    void onEventFromRoutines(String eventName, Array<PropertyWrapper<?>> propertyWrappers);

    void addNodeEventListener(RoutineEventInterface routineEventInterface);
}
