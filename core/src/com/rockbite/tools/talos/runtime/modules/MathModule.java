package com.rockbite.tools.talos.runtime.modules;

import com.rockbite.tools.talos.runtime.Expression;
import com.rockbite.tools.talos.runtime.ParticleSystem;
import com.rockbite.tools.talos.runtime.ScopePayload;
import com.rockbite.tools.talos.runtime.values.FloatValue;
import com.rockbite.tools.talos.runtime.values.NumericalValue;

public class MathModule extends Module {

    public static final int A = 0;
    public static final int B = 1;

    public static final int RESULT = 0;

    FloatValue valA;
    FloatValue valB;

    NumericalValue inputA;
    NumericalValue inputB;
    NumericalValue outputValue;

    private Expression<NumbericalValue> currentExpression;

    @Override
    public void init(ParticleSystem system) {
        super.init(system);

        currentExpression = Expression.sum;

        createInputSlots(2);

        valA = new FloatValue();
        valB = new FloatValue();

        inputValues.put(A, valA);
        inputValues.put(B, valB);
    }

    @Override
    public void processValues(ScopePayload scopePayload) {
        getInputValue(A, scopePayload);
        getInputValue(B, scopePayload);

        if(currentExpression != null) {

            currentExpression.apply(inputA, inputB, outputValue); //Input

            float res = currentExpression.apply(valA.get(), valB.get()); // change expressions to ValueExpressions
            outputValues.get(RESULT).set(res);
        }
    }

    public void setExpression(Expression expression) {
        this.currentExpression = expression;
    }
}
