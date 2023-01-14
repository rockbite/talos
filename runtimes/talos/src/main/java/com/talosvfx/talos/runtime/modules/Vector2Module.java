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

public class Vector2Module extends AbstractModule {

    public static final int X = 0;
    public static final int Y = 1;
    public static final int OUTPUT = 0;

    NumericalValue x;
    NumericalValue y;
    NumericalValue output;

    float defaultX, defaultY;

    @Override
    protected void defineSlots() {
        x = createInputSlot(X);
        y = createInputSlot(Y);

        output = createOutputSlot(OUTPUT);
    }

    @Override
    public void processCustomValues () {

        if(x.isEmpty()) x.set(defaultX);
        if(y.isEmpty()) y.set(defaultY);

        output.set(x, y);
    }

    public void setX(float x) {
        defaultX = x;
    }

    public void setY(float y) {
        defaultY = y;
    }

    public float getDefaultX() {
        return defaultX;
    }

    public float getDefaultY() {
        return defaultY;
    }

    @Override
    public void write (Json json) {
        super.write(json);
        json.writeValue("x", getDefaultX());
        json.writeValue("y", getDefaultY());
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        defaultX = jsonData.getFloat("x", 0);
        defaultY = jsonData.getFloat("y", 0);
    }

}
