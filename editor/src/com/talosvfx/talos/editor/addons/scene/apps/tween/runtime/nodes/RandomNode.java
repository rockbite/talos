package com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.nodes;

import com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.RoutineNode;

import java.util.Arrays;
import java.util.Random;

public class RandomNode extends RoutineNode {

    private float seeds[] = new float[4];

    Random random = new Random();

    @Override
    public Object queryValue(String targetPortName) {

        float seed1 = fetchFloatValue("seed"); // provided seed
        float seed2 = routineInstanceRef.getRequesterId();
        float seed3 = routineInstanceRef.getDepthHash();
        float seed4 = uniqueId; // this id
        seeds[0] = seed1; seeds[1] = seed2; seeds[2] = seed3; seeds[3] = seed4;

        random.setSeed(Arrays.hashCode(seeds));

        float min = fetchFloatValue("min");
        float max = fetchFloatValue("max");

        float rand = min + random.nextFloat() * (max - min);

        return rand;
    }
}
