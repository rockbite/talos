package com.rockbite.tools.talos.runtime.values;


public abstract class Value {

    private boolean isEmpty;

    public void setEmpty(boolean isEmpty) {
        this.isEmpty = isEmpty;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public abstract void set(Value value);
}
