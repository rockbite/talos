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
import com.talosvfx.talos.runtime.Expression;
import com.talosvfx.talos.runtime.utils.MathExpressionMappings;
import com.talosvfx.talos.runtime.values.NumericalValue;

public class MathModule extends AbstractModule {

    public static final int A = 0;
    public static final int B = 1;

    public static final int OUTPUT = 0;

    public NumericalValue a;
    public NumericalValue b;

    float defaultA = 0, defaultB = 0;

    public NumericalValue output;

    private Expression currentExpression = Expression.sum;

    @Override
    protected void defineSlots() {
        a = createInputSlot(A);
        b = createInputSlot(B);

        output = createOutputSlot(OUTPUT);
    }

    @Override
    public void processCustomValues () {
        if(a.isEmpty()) a.set(defaultA);
        if(b.isEmpty()) b.set(defaultB);

        if(currentExpression != null) {
            currentExpression.apply(a, b, output);
        }
    }

    public void setExpression(Expression expression) {
        this.currentExpression = expression;
    }

    public Expression getExpression() {
        return currentExpression;
    }

    public NumericalValue getOutputValue() {
        return output;
    }

    @Override
    public void write (Json json) {
        super.write(json);
        json.writeValue("a", getDefaultA());
        json.writeValue("b", getDefaultB());
        json.writeValue("mathExpression", MathExpressionMappings.getNameForMathExpression(getExpression()));
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        defaultA = jsonData.getFloat("a", 0);
        defaultB = jsonData.getFloat("b", 0);
        currentExpression = MathExpressionMappings.getMathExpressionForName(jsonData.getString("mathExpression"));
    }

    public void setA(float a) {
        defaultA = a;
    }

    public void setB(float b) {
        defaultB = b;
    }

    public float getDefaultA() {
        return defaultA;
    }

    public float getDefaultB() {
        return defaultB;
    }
}
