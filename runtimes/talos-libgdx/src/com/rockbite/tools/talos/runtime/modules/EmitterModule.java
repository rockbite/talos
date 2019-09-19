package com.rockbite.tools.talos.runtime.modules;

import com.rockbite.tools.talos.runtime.ParticleEmitter;
import com.rockbite.tools.talos.runtime.ParticleSystem;
import com.rockbite.tools.talos.runtime.ScopePayload;
import com.rockbite.tools.talos.runtime.values.EmConfigValue;
import com.rockbite.tools.talos.runtime.values.NumericalValue;

public class EmitterModule extends Module {

    public static final int DELAY = 0;
    public static final int DURATION = 1;
    public static final int RATE = 2;
    public static final int CONFIG = 3;


    NumericalValue delay;
    NumericalValue duration;
    NumericalValue rate;
    EmConfigValue config;

    @Override
    public void init(ParticleSystem system) {
        super.init(system);
    }

    @Override
    protected void defineSlots() {
        duration = createInputSlot(DURATION);
        rate = createInputSlot(RATE);

        config = (EmConfigValue) createInputSlot(CONFIG, new EmConfigValue());
    }

    @Override
    public void processValues() {
        // nothing to process
    }

    public float getRate() {
        fetchInputSlotValue(RATE);

        if(rate.isEmpty()) return 50; // defaults

        return rate.getFloat();
    }

    public boolean isAttached() {
        fetchInputSlotValue(CONFIG);

        if(config.isEmpty()) return false;

        return config.attached;
    }

    public boolean isAligned() {
        fetchInputSlotValue(CONFIG);

        if(config.isEmpty()) return false;

        return config.aligned;
    }

    public void updateScopeData(ParticleEmitter particleEmitter) {
        getScope().set(ScopePayload.EMITTER_ALPHA, particleEmitter.alpha);
        getScope().set(ScopePayload.REQUESTER_ID, 1.1f); // TODO change to something more... unique when emitters are in
    }


}
