package com.rockbite.tools.talos.runtime;

import com.badlogic.gdx.utils.Pool;

public class ValuePool extends Pool<Value> {

    @Override
    protected Value newObject() {
        Value value = new Value();
        return value;
    }

    public Value obtainFloat(byte size) {
        return obtain().resetAsFloat(size);
    }

    public Value obtainString() {
        return obtain().resetAsString();
    }

    public Value obtainBoolean() {
        return obtain().resetAsBoolean();
    }
}
