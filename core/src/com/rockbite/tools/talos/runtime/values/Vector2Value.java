package com.rockbite.tools.talos.runtime.values;

import com.badlogic.gdx.math.Vector2;

public class Vector2Value extends Value<Vector2> {

    public Vector2Value() {
        object = new Vector2();
    }

    @Override
    public void set(float value) {
        object.set(value, value);
    }

    @Override
    public void set(Value value) {
        this.object.set((Vector2) value.get());
    }

    @Override
    public void mul(Value value) {
        this.object.scl((Vector2) value.get());
    }

    @Override
    public void add(Value value) {
        this.object.add((Vector2) value.get());
    }

    @Override
    public void reset() {
        this.object.set(0, 0);
    }
}
