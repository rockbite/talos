package com.rockbite.tools.talos.runtime.modules;

import com.badlogic.gdx.math.Interpolation;
import com.rockbite.tools.talos.runtime.ParticleSystem;
import com.rockbite.tools.talos.runtime.ScopePayload;
import com.rockbite.tools.talos.runtime.values.FloatValue;

public class InterpolationModule extends Module {

    public static final int ALPHA = 0;

    FloatValue alphaVal = new FloatValue();

    private Interpolation currentInterpolation;

    @Override
    public void init(ParticleSystem system) {
        super.init(system);

        currentInterpolation = Interpolation.linear;

        createInputSlots(1);
        FloatValue output = new FloatValue();
        outputValues.put(0, output);
    }

    @Override
    public void processValues(ScopePayload scopePayload) {
        getInputValue(alphaVal, ALPHA, scopePayload);
        float alpha = alphaVal.get();

        if(currentInterpolation != null) {
            alphaVal.set(currentInterpolation.apply(alpha));
            outputValues.put(0, alphaVal);
        }
    }

    public void setInterpolation(Interpolation interpolation) {
        this.currentInterpolation = interpolation;
    }
}
