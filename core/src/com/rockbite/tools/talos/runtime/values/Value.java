package com.rockbite.tools.talos.runtime.values;

public abstract class Value<T> {

    protected int flavour;

    protected T object;

    private boolean isEmpty = false;

    public void set(T object) {
        this.object = object;
    }

    public T get() {
        return object;
    }


    public abstract void set(float value);
    public abstract void set(Value value);
    public abstract void mul(Value value);
    public abstract void add(Value value);
    public abstract void reset();

    public void setToDefault() {

    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public void setEmpty(boolean isEmpty) {
        this.isEmpty = isEmpty;
    }
}
