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

public class Vector3Module extends AbstractModule {

    public static final int X = 0;
    public static final int Y = 1;
    public static final int Z = 2;
    public static final int OUTPUT = 0;

    NumericalValue x;
    NumericalValue y;
    NumericalValue z;
    NumericalValue output;

    float defaultX, defaultY, defaultZ;

    @Override
    protected void defineSlots() {
        x = createInputSlot(X);
        y = createInputSlot(Y);
        z = createInputSlot(Z);

        output = createOutputSlot(OUTPUT);
    }

    @Override
    public void processCustomValues () {

        if(x.isEmpty()) x.set(defaultX);
        if(y.isEmpty()) y.set(defaultY);
        if(z.isEmpty()) z.set(defaultZ);

        output.set(x, y, z);
    }

    public void setX(float x) {
        defaultX = x;
    }

    public void setY(float y) {
        defaultY = y;
    }

    public void setZ(float z) {
        defaultZ = z;
    }

    public float getDefaultX() {
        return defaultX;
    }

    public float getDefaultY() {
        return defaultY;
    }

    public float getDefaultZ() {
        return defaultZ;
    }

    @Override
    public void write (Json json) {
        super.write(json);
        json.writeValue("x", getDefaultX());
        json.writeValue("y", getDefaultY());
        json.writeValue("z", getDefaultZ());
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        defaultX = jsonData.getFloat("x", 0);
        defaultY = jsonData.getFloat("y", 0);
        defaultZ = jsonData.getFloat("z", 0);
    }

}
