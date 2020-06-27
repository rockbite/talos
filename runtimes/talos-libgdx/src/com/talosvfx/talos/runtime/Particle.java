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
import com.badlogic.gdx.utils.Pool;
import com.talosvfx.talos.runtime.modules.ParticleModule;

public class Particle implements Pool.Poolable {

    public ParticleEmitterInstance particleEmitter;

    public Vector2 spawnPosition = new Vector2();
    public Vector2 position = new Vector2();
    public float life;
    public float transparency;
    public float rotation;
    public Vector2 size = new Vector2();

    public Color color = new Color();

    public float alpha; // alpha position from 0 to 1 in it's lifetime cycle

    public float seed;

    public float durationAtInit;

    public ParticleDrawable drawable;

    public Particle() {
        // empty constructor
    }

    public void init(ParticleModule particleModule, ParticleEmitterInstance particleEmitter) {
        this.particleEmitter = particleEmitter;

        this.seed = MathUtils.random();

        // inner variable defaults
        alpha = 0f;

        particleModule.updateScopeData(this);

        life = particleModule.getLife(); // really makes more sense like this, for deterministic purposes

        position.set(particleModule.getStartPosition()); // offset
        spawnPosition.set(particleEmitter.getEffect().position);

        durationAtInit = particleEmitter.alpha;
    }

    public void update(float delta) {
        if(alpha == 1f) return;

        //scope data
        ParticleModule particleModule = particleEmitter.emitterGraph.getParticleModule();
        if(particleModule == null) return;

        alpha += delta/life;
        if(alpha > 1f) alpha = 1f;
        particleModule.updateScopeData(this);

        //update variable values
        Vector2 target = particleModule.getTarget();
        float angle = 0;
        if(target == null) {
            angle = particleModule.getAngle(); // do we take angle or target
        } else {
            angle = target.sub(position).angle();
        }

        float velocity = particleModule.getVelocity();
        transparency = particleModule.getTransparency();

        if(particleEmitter.emitterGraph.emitterModule.isAligned()) {
            rotation = angle + particleModule.getRotation();
        } else {
            rotation = particleModule.getRotation();
        }

        drawable = particleModule.getDrawable(); // important to get drawable before size
        particleEmitter.getScope().set(ScopePayload.DRAWABLE_ASPECT_RATIO, drawable.getAspectRatio());

        size.set(particleModule.getSize());
        Vector2 positionOverride = particleModule.getPosition();
        color.set(particleModule.getColor());

        // perform inner operations
        if(positionOverride != null) {
            position.set(positionOverride);
        } else {
            position.x += MathUtils.cosDeg(angle) * velocity * delta;
            position.y += MathUtils.sinDeg(angle) * velocity * delta;
        }
    }

    public float getX() {
        if(particleEmitter.isAttached()) {
            return particleEmitter.getEffect().position.x + position.x;
        } else {
            return spawnPosition.x + position.x;
        }
    }

    public float getY() {
        if(particleEmitter.isAttached()) {
            return particleEmitter.getEffect().position.y + position.y;
        } else {
            return spawnPosition.y + position.y;
        }
    }

    @Override
    public void reset() {

    }
}
