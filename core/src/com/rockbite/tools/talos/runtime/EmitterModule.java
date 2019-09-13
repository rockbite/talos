package com.rockbite.tools.talos.runtime;

import com.rockbite.tools.talos.runtime.values.FloatValue;

public class EmitterModule extends Module {

    public static final int RATE = 0;

    private ScopePayload scopePayload;

    @Override
    public void init(ParticleSystem system) {
        super.init(system);
        scopePayload = new ScopePayload();

        createInputSlots(1);
    }

    @Override
    public void processValues(ScopePayload scopePayload) {
        // nothing to process
    }

    public float getRate() {
        getInputValue(inputValues.get(RATE), RATE, scopePayload);

        if(inputValues.get(RATE).isEmpty()) return 50; // defaults

        return (float) inputValues.get(RATE).get();
    }

    public void updateScopeData(ParticleEmitter particleEmitter) {
        scopePayload.set(ScopePayload.EMITTER_ALPHA, particleEmitter.alpha);
    }
}
