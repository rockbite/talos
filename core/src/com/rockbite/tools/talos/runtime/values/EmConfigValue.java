package com.rockbite.tools.talos.runtime.values;

public class EmConfigValue extends Value {

    public boolean attached;
    public boolean continuous;
    public boolean aligned;
    public boolean additive;

    @Override
    public void set(Value value) {
        set((EmConfigValue) value);
    }

    public void set(EmConfigValue from) {
        this.attached = from.attached;
        this.continuous = from.continuous;
        this.aligned = from.aligned;
        this.additive = from.additive;
    }
}
