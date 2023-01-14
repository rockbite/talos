package com.talosvfx.talos.runtime.modules;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.values.NumericalValue;

public class FakeMotionBlurModule extends AbstractModule {

    public static final int VELOCITY = 0;
    public static final int SIZE = 0;

    NumericalValue velocity;
    NumericalValue size;

    private float velocityMin;
    private float velocityMax;
    private float sizeMin;
    private float sizeMax;

    @Override
    protected void defineSlots() {
        velocity = createInputSlot(VELOCITY);
        size = createOutputSlot(SIZE);
    }

    @Override
    public void processCustomValues () {

        float currVel = MathUtils.clamp(velocity.getFloat(), velocityMin, velocityMax);
        float normVel = (currVel-velocityMin)/(velocityMax-velocityMin); // 0..1
        float sizeVal = sizeMin + (sizeMax - sizeMin) * normVel;

        size.set(sizeVal);

    }

    public float getVelocityMin() {
        return velocityMin;
    }

    public void setVelocityMin(float velocityMin) {
        this.velocityMin = velocityMin;
    }

    public float getVelocityMax() {
        return velocityMax;
    }

    public void setVelocityMax(float velocityMax) {
        this.velocityMax = velocityMax;
    }

    public float getSizeMin() {
        return sizeMin;
    }

    public void setSizeMin(float sizeMin) {
        this.sizeMin = sizeMin;
    }

    public float getSizeMax() {
        return sizeMax;
    }

    public void setSizeMax(float sizeMax) {
        this.sizeMax = sizeMax;
    }

    @Override
    public void write(Json json) {
        super.write(json);
        json.writeValue("velocityMin", velocityMin);
        json.writeValue("velocityMax", velocityMax);
        json.writeValue("sizeMin", sizeMin);
        json.writeValue("sizeMax", sizeMax);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        velocityMin = jsonData.getFloat("velocityMin", 0);
        velocityMax = jsonData.getFloat("velocityMax", 0);
        sizeMin = jsonData.getFloat("sizeMin", 0);
        sizeMax = jsonData.getFloat("sizeMax", 0);
    }
}
