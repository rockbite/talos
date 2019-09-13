package com.rockbite.tools.talos.runtime;

public class StaticValueModule extends Module {

    Value staticValue = new Value();

    @Override
    public void init(ParticleSystem system) {
        super.init(system);

        Value output = new Value();
        outputValues.put(0, output);
    }

    @Override
    public void processValues(ScopePayload scopePayload) {
        outputValues.get(0).set(staticValue);
    }

    public void setStaticValue(float val) {
        staticValue.set(val);
    }
}
