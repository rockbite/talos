package com.rockbite.tools.talos.runtime.modules;

import com.rockbite.tools.talos.runtime.ParticleSystem;
import com.rockbite.tools.talos.runtime.ScopePayload;
import com.rockbite.tools.talos.runtime.values.FloatValue;

public class InputModule extends Module {

    private int scopeKey;

    @Override
    public void init(ParticleSystem system) {
        super.init(system);
    }

    @Override
    public void processValues(ScopePayload scopePayload) {
        scopePayload.get(outputValues.get(0), scopeKey);
    }

    public void setInput(int scopeKey) {
        this.scopeKey = scopeKey;
    }
}
