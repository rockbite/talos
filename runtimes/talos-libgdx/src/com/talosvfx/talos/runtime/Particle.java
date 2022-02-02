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

package com.talosvfx.talos.runtime;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;
import com.talosvfx.talos.runtime.modules.ParticleModule;
import com.talosvfx.talos.runtime.modules.ParticlePointDataGeneratorModule;

public class Particle implements Pool.Poolable {

    private IEmitter emitterReference;

    public Vector3 spawnPosition = new Vector3();
    public Vector3 position = new Vector3();
    public float life;
    public Vector3 rotation = new Vector3();
    public Vector2 size = new Vector2();

    public Vector2 pivot = new Vector2();

    public Color color = new Color();

    public float alpha; // alpha position from 0 to 1 in it's lifetime cycle

    public float seed;
    public int requesterID;

    public float durationAtInit;

    public Particle() {
        // empty constructor
    }

    public void init(IEmitter emitterReference) {
        this.seed = MathUtils.random();
        this.requesterID = emitterReference.getScope().newParticleRequester();
        init(emitterReference, seed);
    }

    public void init(IEmitter emitterReference, float seed) {
        this.emitterReference = emitterReference;
        ParticleModule particleModule = emitterReference.getParticleModule();

        this.seed = seed;

        // inner variable defaults
        alpha = 0f;

        particleModule.updateScopeData(this);

        life = particleModule.getLife(); // really makes more sense like this, for deterministic purposes

        position.set(0, 0, 0); // offset
        spawnPosition.set(emitterReference.getEffectPosition());

        durationAtInit = emitterReference.getAlpha();
    }

    public void update (ParticleEmitterInstance particleEmitterInstance, float delta) {
        if(alpha == 1f) return;

        if(emitterReference == null) return;

        //scope data
        ParticleModule particleModule = emitterReference.getParticleModule();
        if(particleModule == null) return;

        life = particleModule.getLife(); // maybe should remove this

        alpha += delta/life;
        if(alpha > 1f) alpha = 1f;

        applyAlpha(alpha, delta);

        ParticlePointDataGeneratorModule pointDataGenerator = particleModule.getPointDataGenerator();
        if (pointDataGenerator != null) {
            //set the context free the points, and generate new particle point data

            int cacheMode = emitterReference.getScope().getRequestMode();
            int cacheRequestID = emitterReference.getScope().getRequesterID();

            emitterReference.getScope().setCurrentRequestMode(ScopePayload.PARTICLE_ALPHA);
            emitterReference.getScope().setCurrentRequesterID(this.requesterID);
            pointDataGenerator.generateParticlePointData(this, particleEmitterInstance.particlePointDataPool, particleEmitterInstance.groupPool);

            emitterReference.getScope().setCurrentRequestMode(cacheMode);
            emitterReference.getScope().setCurrentRequesterID(cacheRequestID);
        }
    }

    public void applyAlpha (float alpha, float delta) {
        ParticleModule particleModule = emitterReference.getParticleModule();
        if(particleModule == null) return;

        particleModule.updateScopeData(this);

        //update variable values
        float angle = 0;

        if (emitterReference.getEmitterModule().isAligned()) {
            rotation.set(angle, angle, angle).add(particleModule.getRotation());
        } else {
            rotation.set(particleModule.getRotation());
        }

        pivot.set(particleModule.getPivot());
        size.set(1f, 1f);

        Vector3 positionOverride = particleModule.getPosition();
        final boolean positionAddition = particleModule.isPositionAddition();
        // perform inner operations
        if (positionOverride != null) {

            if (positionAddition) {
                float dx = positionOverride.x * delta;
                float dy = positionOverride.y * delta;
                float dz = positionOverride.z * delta;
                position.add(dx, dy, dz);
            } else {
                position.set(positionOverride);
            }
        } else {
//            position.setZero(); //do nothing
        }
    }

    public float getX() {
        if(emitterReference.getEmitterModule().isAttached()) {
            return emitterReference.getEffectPosition().x + position.x;
        } else {
            return spawnPosition.x + position.x;
        }
    }

    public float getY() {
        if(emitterReference.getEmitterModule().isAttached()) {
            return emitterReference.getEffectPosition().y + position.y;
        } else {
            return spawnPosition.y + position.y;
        }
    }

    public float getZ() {
        if(emitterReference.getEmitterModule().isAttached()) {
            return emitterReference.getEffectPosition().z + position.z;
        } else {
            return spawnPosition.z + position.z;
        }
    }

    @Override
    public void reset() {
        position.setZero();
        requesterID = -1;
    }

    public float getEmitterAlpha () {
        return emitterReference.getAlpha();
    }

    public IEmitter getEmitter () {
        return emitterReference;
    }

    public void notifyKill() {
        ParticleModule particleModule = emitterReference.getParticleModule();
        if(particleModule == null) return;
        particleModule.updateScopeData(this);
    }
}
