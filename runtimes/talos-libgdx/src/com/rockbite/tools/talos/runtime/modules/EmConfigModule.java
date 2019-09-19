package com.rockbite.tools.talos.runtime.modules;

import com.rockbite.tools.talos.runtime.ParticleSystem;
import com.rockbite.tools.talos.runtime.values.EmConfigValue;

public class EmConfigModule extends Module {

    public static final int OUTPUT = 0;

    private EmConfigValue userValue = new EmConfigValue();
    private EmConfigValue outputValue;

    @Override
    public void init(ParticleSystem system) {
        super.init(system);

        userValue.attached = false;
        userValue.continuous = true;
        userValue.additive = true;
        userValue.aligned = false;
    }

    @Override
    protected void defineSlots() {
        outputValue = (EmConfigValue) createOutputSlot(OUTPUT, new EmConfigValue());
    }

    @Override
    public void processValues() {
        outputValue.set(userValue);
    }

    public EmConfigValue getUserValue() {
        return userValue;
    }
}
