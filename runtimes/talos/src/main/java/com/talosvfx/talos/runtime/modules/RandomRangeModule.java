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
import com.talosvfx.talos.runtime.utils.DistributedRandom;
import com.talosvfx.talos.runtime.values.NumericalValue;

import java.util.Random;

public class RandomRangeModule extends AbstractModule {

    public static final int MIN_INPUT = 0;
    public static final int MAX_INPUT = 1;

    public static final int OUTPUT = 0;

    NumericalValue minInput;
    NumericalValue maxInput;

    NumericalValue output;

    private float min = 1, max = 1;

    private Random random = new Random();
    private DistributedRandom distributedRandom = new DistributedRandom();
    private boolean distributed = false;

    @Override
    protected void defineSlots() {
        minInput = createInputSlot(MIN_INPUT);
        maxInput = createInputSlot(MAX_INPUT);

        output = createOutputSlot(OUTPUT);
    }

    @Override
    public void processCustomValues () {
        // what's worse, keeping thousands of long values, or keeping floats but casting 1000 times to long?
        // I'll leave the answer to the reader
        long seed = (long) (getScope().getFloat(ScopePayload.PARTICLE_SEED) * 10000 * (index+1) * 1000);

        float startPos;
        if(!distributed) {
            random.setSeed(seed);
            startPos = random.nextFloat();
        } else {
            distributedRandom.setSeed((int) (10000 * (index+1)));
            startPos = distributedRandom.nextFloat();
        }

        float min = this.min;
        float max = this.max;

        if(!minInput.isEmpty()) min = minInput.getFloat();
        if(!maxInput.isEmpty()) max = maxInput.getFloat();

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
        json.writeValue("distributed", distributed);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        min = jsonData.getFloat("min", 0);
        max = jsonData.getFloat("max", 0);
        distributed = jsonData.getBoolean("distributed", false);
    }

    public boolean isDistributed () {
        return distributed;
    }

    public void setDistributed (boolean checked) {
        distributed = checked;
    }
}
