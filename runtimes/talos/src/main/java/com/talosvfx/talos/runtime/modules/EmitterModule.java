/*******************************************************************************
 * Copyright 2019 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.talosvfx.talos.runtime.modules;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.ParticleEmitterInstance;
import com.talosvfx.talos.runtime.ScopePayload;
import com.talosvfx.talos.runtime.values.EmConfigValue;
import com.talosvfx.talos.runtime.values.NumericalValue;

public class EmitterModule extends AbstractModule {

    public static final int DELAY = 0;
    public static final int DURATION = 1;
    public static final int RATE = 2;
    public static final int CONFIG = 3;


    NumericalValue delay;
    NumericalValue duration;
    NumericalValue rate;
    EmConfigValue config;

    public float defaultDelay = 0;
    public float defaultDuration = 2f;
    public float defaultRate = 50f;

    @Override
    protected void defineSlots() {
        delay = createInputSlot(DELAY);
        duration = createInputSlot(DURATION);
        rate = createInputSlot(RATE);

        config = createInputSlot(CONFIG, new EmConfigValue());
    }

    @Override
    public void processCustomValues () {
        // nothing to process
    }



    public float getDelay() {
        fetchInputSlotValue(DELAY);

        if(delay.isEmpty()) return defaultDelay; // defaults

        return delay.getFloat();
    }

    public float getDuration() {
        fetchInputSlotValue(DURATION);

        if(duration.isEmpty()) return defaultDuration; // defaults

        return duration.getFloat();
    }

    public float getRate() {
        fetchInputSlotValue(RATE);

        if(rate.isEmpty()) return defaultRate; // defaults

        return rate.getFloat();
    }

    public boolean isContinuous() {
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

    public boolean isAdditive() {
        fetchInputSlotValue(CONFIG);

        if(config.isEmpty()) return true;

        return config.additive;
    }


    public boolean isBlendAdd () {
        fetchInputSlotValue(CONFIG);

        if(config.isEmpty()) return false;

        return config.isBlendAdd;
    }

    public void updateScopeData(ParticleEmitterInstance particleEmitter) {
        getScope().set(ScopePayload.EMITTER_ALPHA, particleEmitter.alpha);
        getScope().setCurrentRequesterID(ScopePayload.EMITTER_ALPHA);
    }

    @Override
    public void write (Json json) {
        super.write(json);
        json.writeValue("delay", defaultDelay);
        json.writeValue("duration", defaultDuration);
        json.writeValue("rate", defaultRate);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        defaultDelay = jsonData.getFloat("delay", 0);
        defaultDuration = jsonData.getFloat("duration", 2);
        defaultRate = jsonData.getFloat("rate", 50);
    }

    public boolean isImmortal() {
        fetchInputSlotValue(CONFIG);

        if(config.isEmpty()) return false;

        return config.immortal;
    }
}
