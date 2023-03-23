package com.talosvfx.talos.runtime.routine.nodes;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.routine.RoutineNode;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyWrapper;

public class EventNode extends RoutineNode {
    private String eventName;
    private Array<PropertyWrapper<?>> propertyWrappers = new Array<>();
    @Override
    protected void configureNode(JsonValue properties) {
       eventName = properties.getString("eventName");

       configured = true;
    }
}
