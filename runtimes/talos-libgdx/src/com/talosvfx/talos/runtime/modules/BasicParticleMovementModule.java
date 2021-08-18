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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.Particle;
import com.talosvfx.talos.runtime.ScopePayload;
import com.talosvfx.talos.runtime.values.NumericalValue;

public class BasicParticleMovementModule extends AbstractModule {

    public static final int ID = 0;
    public static final int TAG = 1;

    public static final int OFFSET = 3;
    public static final int VELOCITY = 5;
    public static final int GRAVITY = 6;
    public static final int TARGET = 8;
    public static final int ANGLE = 11;
    public static final int MASS = 12;
    public static final int POSITION = 13;


    NumericalValue offset;

    NumericalValue velocity;
    NumericalValue gravity;
    NumericalValue target;
    NumericalValue angle;
    NumericalValue mass;

    NumericalValue position;

    Color tmpColor = new Color();
    Vector2 tmpVec = new Vector2();
    Vector3 tmp3Vec = new Vector3();

    @Override
    protected void defineSlots() {
        offset = createInputSlot(OFFSET);
        velocity = createInputSlot(VELOCITY);
        gravity = createInputSlot(GRAVITY);
        target = createInputSlot(TARGET);
        angle = createInputSlot(ANGLE);
        mass = createInputSlot(MASS);

        position = createOutputSlot(POSITION);

        angle.setFlavour(NumericalValue.Flavour.ANGLE);
    }

    @Override
    public void processValues() {
        // nothing to process, it's all cool as cucumber

        float angle = getAngle();

        float vel = getVelocity();

        position.set(MathUtils.cosDeg(angle) * vel, MathUtils.sinDeg(angle) * vel);
    }

    public void updateScopeData(Particle particle) {
        getScope().set(ScopePayload.EMITTER_ALPHA, particle.getEmitterAlpha());
        getScope().set(ScopePayload.PARTICLE_ALPHA, particle.alpha);
        getScope().set(ScopePayload.PARTICLE_SEED, particle.seed);
        getScope().set(ScopePayload.REQUESTER_ID, particle.seed);
        getScope().set(ScopePayload.EMITTER_ALPHA_AT_P_INIT, particle.durationAtInit);
        getScope().set(ScopePayload.PARTICLE_POSITION, particle.getX(), particle.getY());

        getScope().setParticle(particle);
    }


    public float getAngle() {
        fetchInputSlotValue(ANGLE);
        if(angle.isEmpty()) return 90; // defaults
        return angle.getFloat();
    }

    public float getVelocity() {
        fetchInputSlotValue(VELOCITY);
        if(velocity.isEmpty()) return 0; // defaults
        return velocity.getFloat();
    }


    public Vector2 getStartPosition() {
        fetchInputSlotValue(OFFSET);
        if(offset.isEmpty()) {
            tmpVec.set(0, 0);
            return tmpVec;
        }
        tmpVec.set(offset.get(0), offset.get(1));

        return tmpVec;
    }

    public Vector2 getTarget() {
        fetchInputSlotValue(TARGET);
        if(target.isEmpty()) {
            return null;
        }
        tmpVec.set(target.get(0), target.get(1));

        return tmpVec;
    }


    @Override
    public void write (Json json) {
        super.write(json);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
    }
}
