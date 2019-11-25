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
import com.talosvfx.talos.runtime.ScopePayload;
import com.talosvfx.talos.runtime.values.NumericalValue;

import java.util.Random;

public class RandomRangeModule extends AbstractModule {

    public static final int OUTPUT = 0;

    NumericalValue output;

    private float min = 1, max = 1;

    private Random random = new Random();

    @Override
    protected void defineSlots() {
        output = createOutputSlot(OUTPUT);
    }

    @Override
    public void processValues() {
        random.setSeed((long) ((getScope().getFloat(ScopePayload.PARTICLE_SEED) * 10000 * index * 1000)));
        // what's worse, keeping thousands of long values, or keeping floats but casting 1000 times to long?
        // I'll leave the answer to the reader

        float startPos = random.nextFloat();

        float res = min + (max - min) * startPos;

        output.set(res);
    }

    public void setMinMax(float min, float max) {
        this.min = min;
        this.max = max;
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    public NumericalValue getOutputValue() {
        return output;
    }

    @Override
    public void write (Json json) {
        super.write(json);
        json.writeValue("min", min);
        json.writeValue("max", max);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        min = jsonData.getFloat("min");
        max = jsonData.getFloat("max");
    }

}
