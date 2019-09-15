package com.rockbite.tools.talos.runtime.modules;


import com.rockbite.tools.talos.runtime.Slot;
import com.rockbite.tools.talos.runtime.ParticleSystem;
import com.rockbite.tools.talos.runtime.ScopePayload;
import com.rockbite.tools.talos.runtime.values.FloatValue;
import com.rockbite.tools.talos.runtime.values.Vector2Value;

public class Vector2Module extends Module {

    public static final int X = 0;
    public static final int Y = 1;
    public static final int RESULT = 0;

    Vector2Value output;

    float defaultX, defaultY;

    @Override
    public void init(ParticleSystem system) {
        super.init(system);
    }

    @Override
    protected void defineSlots() {
        createInputSlot(this, X, FloatValue.class);
        createInputSlot(this, Y, FloatValue.class);

        output = createOutputSlot(this, RESULT, Vector2Value.class);
    }

    @Override
    public void processValues() {
        FloatValue valX = getValue(X);
        FloatValue valY = getValue(Y);

        if(valX.isEmpty()) valX.set(defaultX);
        if(valY.isEmpty()) valY.set(defaultY);

        output.set(valX.get(), valY.get());
    }

    public void setX(float x) {
        defaultX = x;
    }

    public void setY(float y) {
        defaultY = y;
    }
}
