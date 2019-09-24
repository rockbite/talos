package com.rockbite.tools.talos.runtime.modules;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.rockbite.tools.talos.runtime.ParticleEmitterInstance;
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
    protected void defineSlots() {
        delay = createInputSlot(DELAY);
        duration = createInputSlot(DURATION);
        rate = createInputSlot(RATE);

        config = (EmConfigValue) createInputSlot(CONFIG, new EmConfigValue());
    }

    @Override
    public void processValues() {
        // nothing to process
    }



    public float getDelay() {
        fetchInputSlotValue(DELAY);

        if(delay.isEmpty()) return 0f; // defaults

        return delay.getFloat();
    }

    public float getDuration() {
        fetchInputSlotValue(DURATION);

        if(duration.isEmpty()) return 2f; // defaults

        return duration.getFloat();
    }

    public float getRate() {
        fetchInputSlotValue(RATE);

        if(rate.isEmpty()) return 50; // defaults

        return rate.getFloat();
    }

    public boolean isContinnuous() {
        fetchInputSlotValue(CONFIG);

        if(config.isEmpty()) return false;

        return config.continuous;
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

    public void updateScopeData(ParticleEmitterInstance particleEmitter) {
        getScope().set(ScopePayload.EMITTER_ALPHA, particleEmitter.alpha);
        getScope().set(ScopePayload.REQUESTER_ID, 1.1f); // TODO change to something more... unique when emitters are in
    }

    @Override
    public void write (Json json) {
        
    }

    @Override
    public void read (Json json, JsonValue jsonData) {

    }
}
