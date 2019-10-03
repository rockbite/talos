package com.rockbite.tools.talos.runtime.modules;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.rockbite.tools.talos.runtime.ScopePayload;
import com.rockbite.tools.talos.runtime.values.NumericalValue;

import java.util.Random;

public class DynamicRangeModule extends CurveModule {

    private float lowMin = 0, lowMax = 0;
    private float highMin = 1, highMax = 1;

    public static final int OUTPUT = 0;

    private Random random = new Random();

    @Override
    public void processValues() {
        processAlphaDefaults();

        // do the random thing first
        float low = calcRandomRange(lowMin, lowMax, 1);
        float high = calcRandomRange(highMin, highMax, 2);

        super.processValues();

        float mix = Interpolation.linear.apply(low, high, output.getFloat());

        output.set(mix);
    }

    private float calcRandomRange(float min, float max, int randomOffset) {

        float a = 23.14069263277926f;
        final float particleSeed = graph.scopePayload.internalMap[ScopePayload.PARTICLE_SEED].elements[0];
        float value = MathUtils.cos(particleSeed * randomOffset * a * index + randomOffset );
        float fraction = value - (int)value;

        float res = min + (max - min) * fraction;

        return res;
    }

    public void setMinMaxHigh(float highMin, float highMax) {
        this.highMin = highMin;
        this.highMax = highMax;
    }

    public void setMinMaxLow(float lowMin, float lowMax) {
        this.lowMin = lowMin;
        this.lowMax = lowMax;
    }

    public float getLowMin() {
        return lowMin;
    }

    public float getLowMax() {
        return lowMax;
    }

    public float getHightMin() {
        return highMin;
    }

    public float getHightMax() {
        return highMax;
    }

    public NumericalValue getOutputValue() {
        return output;
    }

    @Override
    public void write (Json json) {
        super.write(json);
        json.writeValue("lowMin", lowMin, float.class);
        json.writeValue("lowMax", lowMax, float.class);
        json.writeValue("highMin", highMin, float.class);
        json.writeValue("highMax", highMax, float.class);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        lowMin = jsonData.getFloat("lowMin");
        lowMax = jsonData.getFloat("lowMax");
        highMin = jsonData.getFloat("highMin");
        highMax = jsonData.getFloat("highMax");
    }
}
