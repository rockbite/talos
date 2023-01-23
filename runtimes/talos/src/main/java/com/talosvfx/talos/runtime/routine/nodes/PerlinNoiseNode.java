package com.talosvfx.talos.runtime.routine.nodes;

import com.talosvfx.talos.runtime.routine.RoutineNode;
import com.talosvfx.talos.runtime.vfx.utils.SimplexNoise;

public class PerlinNoiseNode extends RoutineNode {

    SimplexNoise noise = new SimplexNoise();

    @Override
    public Object queryValue(String targetPortName) {

        float x = fetchFloatValue("x");
        float y = fetchFloatValue("y");

        float scale = fetchFloatValue("scale");

        float query = (noise.query(x * (30f/256f), y * (30f/256f), scale) + 1f)/2f;

        return query;
    }
}
