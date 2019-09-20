package com.rockbite.tools.talos.runtime.modules;

import com.rockbite.tools.talos.runtime.ScopePayload;
import com.rockbite.tools.talos.runtime.values.NumericalValue;

import java.util.Random;

public class RandomRangeModule extends Module {

    public static final int OUTPUT = 0;

    NumericalValue output;

    private float min = 0, max = 1;

    private Random random = new Random();

    @Override
    protected void defineSlots() {
        output = createOutputSlot(OUTPUT);
    }

    @Override
    public void processValues() {
        random.setSeed((long) ((getScope().getFloat(ScopePayload.PARTICLE_SEED) * 10000 * index * 1000)));
        // what's worse, keeping thousands of long values, or keeping floats but casting 1000 times to long?
        // I'll leave the answer to the reader

        float startPos = random.nextFloat();

        float res = min + (max - min) * startPos;

        output.set(res);
    }

    public void setMinMax(float min, float max) {
        this.min = min;
        this.max = max;
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    public NumericalValue getOutputValue() {
        return output;
    }
}
