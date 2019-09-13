package com.rockbite.tools.talos.runtime;

public class EmitterModule extends Module {

    public static final int RATE = 0;

    private ScopePayload scopePayload;

    Value tmp = new Value();

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
        getInputValue(tmp, RATE, scopePayload);

        if(tmp.isDefault) return 50; // defaults

        return tmp.floatVars[0];
    }

    public void updateScopeData(ParticleEmitter particleEmitter) {
        scopePayload.set(ScopePayload.EMITTER_ALPHA, particleEmitter.alpha);
    }
}
