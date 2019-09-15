package com.rockbite.tools.talos.runtime.modules;

import com.badlogic.gdx.math.Interpolation;
import com.rockbite.tools.talos.runtime.ParticleSystem;
import com.rockbite.tools.talos.runtime.ScopePayload;
import com.rockbite.tools.talos.runtime.values.FloatValue;

public class InterpolationModule extends Module {

    public static final int ALPHA = 0;
    public static final int RESULT = 0;

    FloatValue alpha;

    private Interpolation currentInterpolation;

    @Override
    public void init(ParticleSystem system) {
        super.init(system);

        currentInterpolation = Interpolation.linear;

        createInputSlots(1);
        alpha = new FloatValue();
        inputValues.put(ALPHA, alpha);
    }

    @Override
    public void processValues(ScopePayload scopePayload) {
        getInputValue(ALPHA, scopePayload);

        if(currentInterpolation != null) {
            outputValues.get(RESULT).set(currentInterpolation.apply(alpha.get()));
        }
    }

    public void setInterpolation(Interpolation interpolation) {
        this.currentInterpolation = interpolation;
    }
}
