package com.rockbite.tools.talos.runtime.modules;

import com.rockbite.tools.talos.runtime.ParticleSystem;
import com.rockbite.tools.talos.runtime.ScopePayload;
import com.rockbite.tools.talos.runtime.values.FloatValue;
import com.rockbite.tools.talos.runtime.values.Vector2Value;

public class Vector2Module extends Module {

    public static final int X = 0;
    public static final int Y = 1;

    FloatValue valX = new FloatValue();
    FloatValue valY = new FloatValue();

    float defaultX, defaultY;

    Vector2Value output = new Vector2Value();

    @Override
    public void init(ParticleSystem system) {
        super.init(system);

        createInputSlots(2);

        outputValues.put(0, output);
    }


    @Override
    public void processValues(ScopePayload scopePayload) {
        getInputValue(valX, X, scopePayload);
        getInputValue(valY, Y, scopePayload);

        if(valX.isEmpty()) valX.set(defaultX);
        if(valY.isEmpty()) valY.set(defaultY);

        output.set(valX.getFloat(), valY.getFloat());
    }

    public void setX(float x) {
        defaultX = x;
    }

    public void setY(float y) {
        defaultY = y;
    }
}
