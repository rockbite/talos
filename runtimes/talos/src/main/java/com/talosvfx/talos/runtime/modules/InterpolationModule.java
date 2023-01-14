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
import com.talosvfx.talos.runtime.utils.InterpolationMappings;
import com.talosvfx.talos.runtime.values.NumericalValue;

public class InterpolationModule extends AbstractModule {

    public static final int ALPHA = 0;
    public static final int OUTPUT = 0;

    NumericalValue alpha;
    NumericalValue output;

    private Interpolation currentInterpolation = Interpolation.linear;

    @Override
    protected void defineSlots() {
        alpha = createAlphaInputSlot(ALPHA);

        output = createOutputSlot(OUTPUT);
    }

    @Override
    public void processCustomValues () {
        output.set(currentInterpolation.apply(alpha.getFloat()));
    }

    public void setInterpolation(Interpolation interpolation) {
        this.currentInterpolation = interpolation;
    }

    public Interpolation getInterpolation() {
        return this.currentInterpolation;
    }

    @Override
    public void write (Json json) {
        super.write(json);
        json.writeValue("interp", InterpolationMappings.getNameForInterpolation(getInterpolation()));
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        currentInterpolation = InterpolationMappings.getInterpolationForName(jsonData.getString("interp"));
    }

}
