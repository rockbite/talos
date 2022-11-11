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
import com.talosvfx.talos.runtime.utils.SimplexNoise;
import com.talosvfx.talos.runtime.values.NumericalValue;

public class NoiseModule extends AbstractModule {

    public static final int X = 0;
    public static final int Y = 1;
    public static final int OUTPUT = 0;

    NumericalValue x;
    NumericalValue y;
    NumericalValue output;

    SimplexNoise noise = new SimplexNoise();

    public float frequency = 20f;

    @Override
    protected void defineSlots() {
        x = createInputSlot(X);
        y = createInputSlot(Y);

        output = createOutputSlot(OUTPUT);
    }

    @Override
    public void processCustomValues () {
        output.set(noiseFunction(x.getFloat(), y.getFloat()));
    }

    private float noiseFunction(float x, float y) {
        // normalize
        x = x - (int)x;
        y = y - (int)y;

        float particleSeed = getScope().getFloat(ScopePayload.PARTICLE_SEED);
        y = y * particleSeed;
        y = y - (int)y;

        float result = noise.query(x, y, frequency);

        //result = result * 0.5f + 0.5f; // bring to 0-1 range (actually no not needed)

        return result;
    }

    public void setFrequency(float frequency) {
        this.frequency = frequency;
    }

    public float getFrequency() {
        return frequency;
    }


    @Override
    public void write (Json json) {
        super.write(json);
        json.writeValue("frequency", getFrequency());
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        setFrequency(jsonData.getFloat("frequency", 20f));
    }
}
