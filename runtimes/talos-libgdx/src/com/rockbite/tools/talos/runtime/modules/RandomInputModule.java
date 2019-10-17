package com.rockbite.tools.talos.runtime.modules;

import com.badlogic.gdx.math.MathUtils;
import com.rockbite.tools.talos.runtime.ScopePayload;
import com.rockbite.tools.talos.runtime.Slot;
import com.rockbite.tools.talos.runtime.values.Value;

import java.util.Random;

public class RandomInputModule extends Module {

    private Random random = new Random();

    @Override
    protected void defineSlots() {
        addInputSlot(0);
        createOutputSlot(0, null);
    }

    public void addInputSlot(int key) {
        Slot slot = new Slot(this, key, true);
        inputSlots.put(key, slot);
    }

    @Override
    public void processValues() {

        Value output = outputSlots.get(0).getValue();
        if(output != null) {
            random.setSeed((long) ((getScope().getFloat(ScopePayload.EMITTER_ALPHA) * 10000 * (index+1) * 1000)));
            int index = MathUtils.floor(random.nextFloat() * (inputSlots.size - 1));

            Value input = inputSlots.get(index).getValue();
            if(input != null) {
                output.set(input);
            }
        }
    }
}
