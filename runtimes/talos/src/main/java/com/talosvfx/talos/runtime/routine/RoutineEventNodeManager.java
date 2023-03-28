package com.talosvfx.talos.runtime.routine;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyWrapper;

public interface RoutineEventNodeManager {
    void post(String eventName, Array<PropertyWrapper<?>> propertires);

    void addListener(RoutineEventNodeListener listener);
}
