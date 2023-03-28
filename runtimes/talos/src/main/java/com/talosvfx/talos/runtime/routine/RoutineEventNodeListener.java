package com.talosvfx.talos.runtime.routine;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyWrapper;

public interface RoutineEventNodeListener {
    void receive(String eventName, Array<PropertyWrapper<?>> propertyWrappers);
}
