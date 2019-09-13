package com.rockbite.tools.talos.runtime.values;

public class FloatValue extends Value<Float> {

    public FloatValue() {
        object = 0f;
    }

    @Override
    public void set(float value) {
        object = value;
    }


    public float getFloat() {
        return object;
    }

    @Override
    public void set(Value value) {
        this.object = (Float) value.get();
    }

    @Override
    public void mul(Value value) {
        this.object = this.object * (Float) value.get();
    }

    @Override
    public void add(Value value) {
        this.object = this.object + (Float) value.get();
    }

    @Override
    public void reset() {
        this.object = 0f;
    }
}
