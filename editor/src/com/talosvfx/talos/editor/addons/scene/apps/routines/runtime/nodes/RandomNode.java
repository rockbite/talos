package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes;

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
