package com.rockbite.tools.talos.runtime.modules;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.rockbite.tools.talos.runtime.Expression;
import com.rockbite.tools.talos.runtime.utils.MathExpressionMappings;
import com.rockbite.tools.talos.runtime.values.NumericalValue;

public class MathModule extends Module {

    public static final int A = 0;
    public static final int B = 1;

    public static final int OUTPUT = 0;

    public NumericalValue a;
    public NumericalValue b;

    public NumericalValue output;

    private Expression currentExpression = Expression.sum;

    @Override
    protected void defineSlots() {
        a = createInputSlot(A);
        b = createInputSlot(B);

        output = createOutputSlot(OUTPUT);
    }

    @Override
    public void processValues() {
        if(a.isEmpty) a.set(0);
        if(b.isEmpty) b.set(0);

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
        json.writeValue("mathExpression", MathExpressionMappings.getNameForMathExpression(getExpression()));
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        currentExpression = MathExpressionMappings.getMathExpressionForName(jsonData.getString("mathExpression"));
    }

}
