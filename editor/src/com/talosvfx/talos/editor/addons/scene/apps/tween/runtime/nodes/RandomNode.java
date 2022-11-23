package com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.nodes;

import com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.RoutineNode;

import java.util.Arrays;
import java.util.Random;

public class RandomNode extends ARndNode {

    @Override
    public Object queryValue(String targetPortName) {

        setSeed();

        float min = fetchFloatValue("min");
        float max = fetchFloatValue("max");

        float rand = min + random.nextFloat() * (max - min);

        return rand;
    }
}
