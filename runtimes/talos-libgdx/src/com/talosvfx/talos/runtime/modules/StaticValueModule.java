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
import com.talosvfx.talos.runtime.values.NumericalValue;

public class StaticValueModule extends AbstractModule {

    public static final int OUTPUT = 0;

    private NumericalValue staticValue;
    private NumericalValue outputValue;

    @Override
    protected void defineSlots() {
        outputValue = createOutputSlot(OUTPUT);

        staticValue = new NumericalValue();
        staticValue.set(1f);
    }

    @Override
    public void processCustomValues () {
        outputValue.set(staticValue);
    }

    public void setStaticValue(float val) {
        staticValue.set(val);
    }

    public float getStaticValue() {
        return staticValue.getFloat();
    }

    public NumericalValue getOutputValue() {
        return outputValue;
    }

    @Override
    public void write (Json json) {
        super.write(json);
        json.writeValue("value", getStaticValue());
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        setStaticValue(jsonData.getFloat("value"));
    }

}
