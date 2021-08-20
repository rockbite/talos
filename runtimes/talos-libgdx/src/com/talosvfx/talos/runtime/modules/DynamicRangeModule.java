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

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.ScopePayload;
import com.talosvfx.talos.runtime.values.NumericalValue;

import java.util.Random;

public class DynamicRangeModule extends CurveModule {

    private float lowMin = 0, lowMax = 0;
    private float highMin = 1, highMax = 1;

    public static final int OUTPUT = 0;

    private Random random = new Random();

    @Override
    public void processCustomValues () {

        // do the random thing first
        float low = calcRandomRange(lowMin, lowMax, 1);
        float high = calcRandomRange(highMin, highMax, 2);

        super.processCustomValues();

        float mix = Interpolation.linear.apply(low, high, output.getFloat());

        output.set(mix);
    }

    private float calcRandomRange(float min, float max, int randomOffset) {
        random.setSeed((long) ((getScope().getFloat(ScopePayload.PARTICLE_SEED) * 10000 * (index + randomOffset) * 1000)));
        float startPos = random.nextFloat();
        float res = min + (max - min) * startPos;

        return res;
    }

    public void setMinMaxHigh(float highMin, float highMax) {
        this.highMin = highMin;
        this.highMax = highMax;
    }

    public void setMinMaxLow(float lowMin, float lowMax) {
        this.lowMin = lowMin;
        this.lowMax = lowMax;
    }

    public float getLowMin() {
        return lowMin;
    }

    public float getLowMax() {
        return lowMax;
    }

    public float getHightMin() {
        return highMin;
    }

    public float getHightMax() {
        return highMax;
    }

    public NumericalValue getOutputValue() {
        return output;
    }

    @Override
    public void write (Json json) {
        super.write(json);
        json.writeValue("lowMin", lowMin, float.class);
        json.writeValue("lowMax", lowMax, float.class);
        json.writeValue("highMin", highMin, float.class);
        json.writeValue("highMax", highMax, float.class);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        lowMin = jsonData.getFloat("lowMin");
        lowMax = jsonData.getFloat("lowMax");
        highMin = jsonData.getFloat("highMin");
        highMax = jsonData.getFloat("highMax");
    }
}
