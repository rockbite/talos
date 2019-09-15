package com.rockbite.tools.talos.runtime.values;

import com.badlogic.gdx.math.Interpolation;

public abstract class Value<T> {

    public static final Class[] FLOAT  = new Class[]{FloatValue.class};
    public static final Class[] NUMERIC  = new Class[]{FloatValue.class, Vector2Value.class, ColorValue.class};

    protected int flavour;

    protected T object;

    private boolean isEmpty = false;

    public void set(T object) {
        this.object = object;
    }

    public T get() {
        return object;
    }


    public abstract Value set(float value);
    public abstract Value set(Value value);
    public abstract Value mul(Value value);
    public abstract Value div(Value value);
    public abstract Value add(Value value);
    public abstract Value sub(Value value);
    public abstract Value reset();

    public void setToDefault() {

    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public void setEmpty(boolean isEmpty) {
        this.isEmpty = isEmpty;
    }
}
