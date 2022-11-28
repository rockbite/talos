package com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.nodes;

import com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.RoutineNode;

import java.util.Arrays;
import java.util.Random;

public abstract class ARndNode extends RoutineNode {

    private float seeds[] = new float[4];
    Random random = new Random();

    protected void setSeed() {
        int oldSeed = Arrays.hashCode(seeds);
        float seed1 = 0;
        if(inputs.containsKey("seed")) {
            seed1 = fetchFloatValue("seed"); // provided seed
        }
        float seed2 = routineInstanceRef.getRequesterId();
        float seed3 = routineInstanceRef.getDepthHash();
        float seed4 = uniqueId; // this id
        seeds[0] = seed1; seeds[1] = seed2; seeds[2] = seed3; seeds[3] = seed4;
        int seed = Arrays.hashCode(seeds);
        random.setSeed(seed);
        if (oldSeed != seed) {
            clearCache();
        }
    }

}
