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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntMap;
import com.talosvfx.talos.runtime.values.NumericalValue;

public class ScopePayload {

    public static final int EMITTER_ALPHA = 0;
    public static final int PARTICLE_ALPHA = 1;
    public static final int PARTICLE_SEED = 2;
    public static final int REQUESTER_ID = 3;
    public static final int EMITTER_ALPHA_AT_P_INIT = 4;
    public static final int DRAWABLE_ASPECT_RATIO = 5;
    public static final int SECONDARY_SEED = 6;
    public static final int TOTAL_TIME = 7;
    public static final int PARTICLE_POSITION = 8;

    private Particle processingParticleRef;

    private IntMap<NumericalValue> map = new IntMap<>();

    private IntMap<NumericalValue> dynamicValues = new IntMap<>();

    public ScopePayload() {
        for(int i = 0; i < 10; i++) {
            map.put(i, new NumericalValue());
        }
        for(int i = 0; i < 10; i++) {
            dynamicValues.put(i, new NumericalValue());
        }
    }

    public void set(int index, float value) {
        map.get(index).set(value);
    }

    public void setParticle(Particle particle) {
        processingParticleRef = particle;
    }

    public Particle currParticle() {
        return processingParticleRef;
    }

    public void set(int index, float x, float y) {
        map.get(index).set(x, y);
    }

    public void set(int index, Vector2 value) {
        map.get(index).set(value.x, value.y);
    }

    public void set(int index, NumericalValue value) {
        map.get(index).set(value);
    }

    public NumericalValue get(int index) {
        return map.get(index);
    }

    public float getFloat(int index) {
        return map.get(index).getFloat();
    }

    public void reset() {
        for(int i = 0; i < 10; i++) {
            map.get(i).set(0);
        }
    }

    public NumericalValue getDynamicValue(int key) {
        return dynamicValues.get(key);
    }

    public void setDynamicValue(int key, float val) {
        dynamicValues.get(key).set(val);
    }

    public void setDynamicValue(int key, Vector2 val) {
        dynamicValues.get(key).set(val.x, val.y);
    }

    public void setDynamicValue(int key, Color val) {
        dynamicValues.get(key).set(val.r, val.g, val.b);
    }

    public void setDynamicValue(int key, Vector3 val) {
        dynamicValues.get(key).set(val.x, val.y, val.z);
    }

    public void setDynamicValue(int key, NumericalValue val) {
        dynamicValues.get(key).set(val);
    }
}
