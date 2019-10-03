package com.rockbite.tools.talos.runtime.modules;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.rockbite.tools.talos.runtime.ScopePayload;
import com.rockbite.tools.talos.runtime.values.NumericalValue;

import java.util.Random;

public class RandomRangeModule extends Module {

    public static final int OUTPUT = 0;

    NumericalValue output;

    private float min = 1, max = 1;

    private Random random = new Random();

    @Override
    protected void defineSlots() {
        output = createOutputSlot(OUTPUT);
    }

    @Override
    public void processValues() {
        float a = 23.14069263277926f;
        final float particleSeed = graph.scopePayload.internalMap[ScopePayload.PARTICLE_SEED].elements[0];
        float value = MathUtils.cos(particleSeed * a * index);
        float fraction = value - (int)value;

        float res = min + (max - min) * fraction;

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

    @Override
    public void write (Json json) {
        super.write(json);
        json.writeValue("min", min);
        json.writeValue("max", max);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        min = jsonData.getFloat("min");
        max = jsonData.getFloat("max");
    }

}
