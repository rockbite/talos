package com.rockbite.tools.talos.runtime.modules;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.rockbite.tools.talos.runtime.values.NumericalValue;

public class MixModule extends Module {

    public static final int ALPHA = 0;
    public static final int VAL1 = 1;
    public static final int VAL2 = 2;

    public static final int OUTPUT = 0;

    NumericalValue alpha;
    NumericalValue val1;
    NumericalValue val2;
    NumericalValue output;

    @Override
    protected void defineSlots() {
        alpha = createInputSlot(ALPHA);
        val1 = createInputSlot(VAL1);
        val2 = createInputSlot(VAL2);

        output = createOutputSlot(OUTPUT);
    }

    @Override
    public void processValues() {
        int count = Math.max(val1.currentElementCount, val2.currentElementCount);
        for(int i = 0; i < count; i++) {
            output.elements[i] = Interpolation.linear.apply(val1.elements[i], val2.elements[i], alpha.elements[0]);
        }
        output.setElementsCount(count);
    }

    @Override
    public void write (Json json) {
        super.write(json);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
    }
}
