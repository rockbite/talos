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

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.values.NumericalValue;

/**
 * Calculates the position, rotatoin and length of particle beam based on from and to vector values
 */
public class FromToModule extends AbstractModule {

    public static final int FROM = 0;
    public static final int TO = 1;

    public static final int POSITION = 0;
    public static final int ROTATION = 1;
    public static final int LENGTH = 2;

    public NumericalValue from;
    public NumericalValue to;

    NumericalValue position;
    NumericalValue rotation;
    NumericalValue length;

    Vector3 tmpVec = new Vector3();

    public Vector3 defaultFrom = new Vector3();
    public Vector3 defaultTo = new Vector3();

    @Override
    protected void defineSlots() {
        from = createInputSlot(FROM);
        to = createInputSlot(TO);

        position = createOutputSlot(POSITION);
        rotation = createOutputSlot(ROTATION);
        length = createOutputSlot(LENGTH);
    }

    @Override
    public void processCustomValues () {
        if(from.isEmpty()) from.set(defaultFrom.x, defaultFrom.y);
        if(to.isEmpty()) to.set(defaultTo.x, defaultTo.y);

        tmpVec.set(to.get(0), to.get(1), to.get(2));
        tmpVec.sub(from.get(0), from.get(1), from.get(2));

        position.set(from.get(0) + tmpVec.x/2f, from.get(1) + tmpVec.y/2f);


        //Legacy
//        rotation.set(tmpVec.angle());

        length.set(tmpVec.len());
    }

    public void setDefaults (Vector3 dFrom, Vector3 dTo) {
        defaultFrom.set(dFrom);
        defaultTo.set(dTo);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);

        defaultFrom.x = jsonData.getFloat("fromX", 0);
        defaultFrom.y = jsonData.getFloat("fromY", 0);
        defaultTo.x = jsonData.getFloat("toX", 0);
        defaultTo.y = jsonData.getFloat("toY", 0);
    }

    @Override
    public void write(Json json) {
        super.write(json);

        json.writeValue("fromX", defaultFrom.x);
        json.writeValue("fromY", defaultFrom.y);
        json.writeValue("toX", defaultTo.x);
        json.writeValue("toY", defaultTo.y);
    }
}
