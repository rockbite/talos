package com.rockbite.tools.talos.runtime;

public class InputModule extends Module {

    private int scopeKey;

    @Override
    public void init(ParticleSystem system) {
        super.init(system);

        Value output = new Value();
        outputValues.put(0, output);
    }

    @Override
    public void processValues(ScopePayload scopePayload) {
        scopePayload.get(outputValues.get(0), scopeKey);
    }

    public void setInput(int scopeKey) {
        this.scopeKey = scopeKey;
    }
}
