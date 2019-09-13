package com.rockbite.tools.talos.runtime;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.IntMap;

public class InterpolationModule extends Module {

    public static final int ALPHA = 0;

    Value alphaVal = new Value();

    private Interpolation currentInterpolation;

    @Override
    public void init(ParticleSystem system) {
        super.init(system);

        currentInterpolation = Interpolation.linear;

        createInputSlots(1);
        Value output = new Value();
        outputValues.put(0, output);
    }

    @Override
    public void processValues(ScopePayload scopePayload) {
        getInputValue(alphaVal, ALPHA, scopePayload);
        float alpha = alphaVal.floatVars[0];

        if(currentInterpolation != null) {
            alphaVal.floatVars[0] = currentInterpolation.apply(alpha);
            outputValues.put(0, alphaVal);
        }
    }

    public void setInterpolation(Interpolation interpolation) {
        this.currentInterpolation = interpolation;
    }
}
