package com.rockbite.tools.talos.runtime;

import com.badlogic.gdx.math.Interpolation;

public class MixModule extends Module {

    public static final int ALPHA = 0;
    public static final int VAL1 = 1;
    public static final int VAL2 = 2;

    Value alphaV = new Value();
    Value val1V = new Value();
    Value val2V = new Value();

    @Override
    public void init(ParticleSystem system) {
        super.init(system);

        createInputSlots(3);
        Value output = new Value();
        outputValues.put(0, output);
    }

    @Override
    public void processValues(ScopePayload scopePayload) {
        getInputValue(alphaV, ALPHA, scopePayload);
        getInputValue(val1V, VAL1, scopePayload);
        getInputValue(val2V, VAL2, scopePayload);

        float alpha = alphaV.floatVars[0];
        float val1 = val1V.floatVars[0];
        float val2 = val2V.floatVars[0];

        float result = Interpolation.linear.apply(val1, val2, alpha);

        outputValues.get(0).floatVars[0] = result;
    }
}
