package com.talosvfx.talos.runtime.routine.nodes;

import com.badlogic.gdx.math.Vector2;
import com.talosvfx.talos.runtime.routine.RoutineNode;

public class Vector2Node extends RoutineNode {

    Vector2 vec = new Vector2();

    @Override
    public Object queryValue(String targetPortName) {
        float x = fetchFloatValue("x");
        float y = fetchFloatValue("y");

        vec.set(x, y);

        return vec;
    }
}
