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

public class InputModule extends AbstractModule {

    public static final int OUTPUT = 0;
    private NumericalValue outputValue;

    private int scopeKey;

    @Override
    protected void defineSlots() {
        outputValue = createOutputSlot(OUTPUT);
    }

    @Override
    public void processCustomValues () {
        NumericalValue value = getScope().get(scopeKey);
        outputValue.set(value);
    }

    public void setInput(int scopeKey) {
        this.scopeKey = scopeKey;
    }

    public int getInput() {
        return this.scopeKey;
    }

    @Override
    public void write (Json json) {
        super.write(json);
        json.writeValue("scopeKey", getInput(), int.class);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        setInput(jsonData.getInt("scopeKey"));
    }
}
