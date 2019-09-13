package com.rockbite.tools.talos.runtime.modules;

import com.rockbite.tools.talos.runtime.Expression;
import com.rockbite.tools.talos.runtime.ParticleSystem;
import com.rockbite.tools.talos.runtime.ScopePayload;
import com.rockbite.tools.talos.runtime.values.FloatValue;

public class MathModule extends Module {

    public static final int A = 0;
    public static final int B = 1;

    FloatValue valA = new FloatValue();
    FloatValue valB = new FloatValue();

    private Expression currentExpression;

    @Override
    public void init(ParticleSystem system) {
        super.init(system);

        currentExpression = Expression.sum;

        createInputSlots(3);
        FloatValue output = new FloatValue();
        outputValues.put(0, output);
    }

    @Override
    public void processValues(ScopePayload scopePayload) {
        getInputValue(valA, A, scopePayload);
        getInputValue(valB, B, scopePayload);

        if(currentExpression != null) {
            float res = currentExpression.apply(valA.get(), valB.get()); // change expressions to ValueExpressions
            outputValues.get(0).set(res);
        }
    }

    public void setExpression(Expression expression) {
        this.currentExpression = expression;
    }
}
