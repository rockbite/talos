package com.talosvfx.talos.runtime.routine.nodes;

import com.badlogic.gdx.math.Vector2;
import com.talosvfx.talos.runtime.routine.RoutineNode;

public class Vector2Split extends RoutineNode {

    @Override
    public Object queryValue(String targetPortName) {

        Vector2 vector2 = (Vector2) fetchValue("vector2");

        if(vector2 != null) {

            if (targetPortName.equals("x")) {
                return vector2.x;
            } else if (targetPortName.equals("y")) {
                return vector2.y;
            }
        }

        return 0;
    }
}
