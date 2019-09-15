package com.rockbite.tools.talos.runtime.modules;

import com.rockbite.tools.talos.runtime.ParticleEmitter;
import com.rockbite.tools.talos.runtime.ParticleSystem;
import com.rockbite.tools.talos.runtime.ScopePayload;
import com.rockbite.tools.talos.runtime.values.FloatValue;

public class EmitterModule extends Module {

    public static final int RATE = 0;

    private ScopePayload scopePayload;

    @Override
    public void init(ParticleSystem system) {
        super.init(system);
        scopePayload = new ScopePayload();
    }

    @Override
    protected void defineSlots() {
        createInputSlot(this, RATE, FloatValue.class);
    }

    @Override
    public void processValues() {
        // nothing to process
    }

    public float getRate() {
        FloatValue rate = getValue(RATE);
        if(rate.isEmpty()) return 50; // defaults

        return rate.get();
    }

    public void updateScopeData(ParticleEmitter particleEmitter) {
        scopePayload.set(ScopePayload.EMITTER_ALPHA, particleEmitter.alpha);
    }
}
