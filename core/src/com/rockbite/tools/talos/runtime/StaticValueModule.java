package com.rockbite.tools.talos.runtime;

import com.rockbite.tools.talos.runtime.values.FloatValue;
import com.rockbite.tools.talos.runtime.values.Value;
import com.rockbite.tools.talos.runtime.values.Vector2Value;

public class StaticValueModule extends Module {

    private Value staticValue;

    @Override
    public void init(ParticleSystem system) {
        super.init(system);
    }

    @Override
    public void attached(Module module, int slot) {
        super.attached(module, slot);

        //check the type of connected module slot
        if(module.inputValues.get(slot) instanceof FloatValue) {
            staticValue = new FloatValue();
        } else if(module.inputValues.get(slot) instanceof Vector2Value) {
            staticValue = new Vector2Value();
        } else {
            staticValue = new FloatValue();
        }
        outputValues.put(0, staticValue);
    }

    @Override
    public void processValues(ScopePayload scopePayload) {
        outputValues.get(0).set(staticValue);
    }

    public void setStaticValue(float val) {
        if(staticValue == null) staticValue = new FloatValue();
        staticValue.set(val);
    }
}
