package com.rockbite.tools.talos.runtime.modules;

import com.rockbite.tools.talos.runtime.Expression;
import com.rockbite.tools.talos.runtime.ScopePayload;
import com.rockbite.tools.talos.runtime.values.NumericalValue;

public class MathModule extends Module {

    public static final int A = 0;
    public static final int B = 1;

    public static final int OUTPUT = 0;

    NumericalValue a;
    NumericalValue b;

    NumericalValue output;

    private Expression currentExpression;

    @Override
    protected void defineSlots() {
        a = createInputSlot(A);
        b = createInputSlot(B);

        output = createOutputSlot(OUTPUT);
    }

    @Override
    public void processValues() {
        if(currentExpression != null) {
            currentExpression.apply(a, b, output);
        }
    }

    public void setExpression(Expression expression) {
        this.currentExpression = expression;
    }
}
