package com.rockbite.tools.talos.runtime.modules;

import com.badlogic.gdx.math.Interpolation;
import com.rockbite.tools.talos.runtime.ParticleSystem;
import com.rockbite.tools.talos.runtime.ScopePayload;
import com.rockbite.tools.talos.runtime.values.FloatValue;
import com.rockbite.tools.talos.runtime.values.Value;

public class MixModule extends Module {

    public static final int ALPHA = 0;
    public static final int VAL1 = 1;
    public static final int VAL2 = 2;

    public static final int OUTPUT = 0;

    @Override
    public void init(ParticleSystem system) {
        super.init(system);
    }

    @Override
    protected void defineSlots() {
        createInputSlot(this, ALPHA, Value.FLOAT);
        createInputSlot(this, VAL1, Value.NUMERIC);
        createInputSlot(this, VAL2, Value.NUMERIC);

        createOutputSlot(this, OUTPUT, Value.NUMERIC);
    }

    @Override
    public void processValues() {
        FloatValue alphaV = getValueFromInputSlot(ALPHA, FloatValue.class);

        Value val1 = getValueFromInputSlot(VAL1, Value.class);
        Value val2 = getValueFromInputSlot(VAL2, Value.class);

        float alpha = alphaV.get();

        Value output = getOutputContainer(OUTPUT);
        output.set(val2).sub(val1).mul(alpha).add(val1);

    }
}
