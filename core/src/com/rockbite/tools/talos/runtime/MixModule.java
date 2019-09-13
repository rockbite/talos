package com.rockbite.tools.talos.runtime;

import com.badlogic.gdx.math.Interpolation;
import com.rockbite.tools.talos.runtime.values.FloatValue;
import com.rockbite.tools.talos.runtime.values.Value;

public class MixModule extends Module {

    public static final int ALPHA = 0;
    public static final int VAL1 = 1;
    public static final int VAL2 = 2;

    FloatValue alphaV = new FloatValue();
    FloatValue val1V = new FloatValue();
    FloatValue val2V = new FloatValue();

    @Override
    public void init(ParticleSystem system) {
        super.init(system);

        createInputSlots(3);
        FloatValue output = new FloatValue();
        outputValues.put(0, output);
    }

    @Override
    public void processValues(ScopePayload scopePayload) {
        getInputValue(alphaV, ALPHA, scopePayload);
        getInputValue(val1V, VAL1, scopePayload);
        getInputValue(val2V, VAL2, scopePayload);

        float alpha = alphaV.get();
        float val1 = val1V.get();
        float val2 = val2V.get();

        float result = Interpolation.linear.apply(val1, val2, alpha);

        outputValues.get(0).set(result);
    }
}
