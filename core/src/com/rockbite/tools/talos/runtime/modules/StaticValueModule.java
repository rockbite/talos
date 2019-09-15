package com.rockbite.tools.talos.runtime.modules;

import com.rockbite.tools.talos.runtime.ParticleSystem;
import com.rockbite.tools.talos.runtime.ScopePayload;
import com.rockbite.tools.talos.runtime.values.FloatValue;
import com.rockbite.tools.talos.runtime.values.Value;

public class StaticValueModule extends Module {

    private FloatValue staticValue = new FloatValue();
    private FloatValue outputValue;

    @Override
    public void init(ParticleSystem system) {
        super.init(system);
    }

    @Override
    protected void defineSlots() {
        outputValue = createOutputSlot(this, 0, FloatValue.class);
    }

    @Override
    public void processValues() {
        outputValue.set(staticValue);
    }

    public void setStaticValue(float val) {
        staticValue.set(val);
    }
}
