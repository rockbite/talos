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
import com.talosvfx.talos.runtime.values.NumericalValue;

public class MixModule extends AbstractModule {

    public static final int ALPHA = 0;
    public static final int VAL1 = 1;
    public static final int VAL2 = 2;

    public static final int OUTPUT = 0;

    NumericalValue alpha;
    NumericalValue val1;
    NumericalValue val2;
    NumericalValue output;

    @Override
    protected void defineSlots() {
        alpha = createAlphaInputSlot(ALPHA);
        val1 = createInputSlot(VAL1);
        val2 = createInputSlot(VAL2);

        output = createOutputSlot(OUTPUT);
    }

    @Override
    public void processCustomValues () {
        int count = Math.max(val1.elementsCount(), val2.elementsCount());
        for(int i = 0; i < count; i++) {
            output.getElements()[i] = Interpolation.linear.apply(val1.getElements()[i], val2.getElements()[i], alpha.getFloat());
        }
        output.setElementsCount(count);
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
