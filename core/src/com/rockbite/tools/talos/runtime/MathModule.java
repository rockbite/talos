package com.rockbite.tools.talos.runtime;

public class MathModule extends Module {

    public static final int A = 0;
    public static final int B = 1;

    Value valA = new Value();
    Value valB = new Value();

    private Expression currentExpression;

    @Override
    public void init(ParticleSystem system) {
        super.init(system);

        currentExpression = Expression.sum;

        createInputSlots(3);
        Value output = new Value();
        outputValues.put(0, output);
    }

    @Override
    public void processValues(ScopePayload scopePayload) {
        getInputValue(valA, A, scopePayload);
        getInputValue(valB, B, scopePayload);

        if(currentExpression != null) {
            for(int i = 0; i < 3; i++){
                outputValues.get(0).floatVars[i] = currentExpression.apply(valA.floatVars[i], valB.floatVars[i]);
                outputValues.put(0, outputValues.get(0));
            }
        }
    }

    public void setExpression(Expression expression) {
        this.currentExpression = expression;
    }
}
