package com.rockbite.tools.talos.runtime.modules;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.rockbite.tools.talos.runtime.ParticleSystem;
import com.rockbite.tools.talos.runtime.ScopePayload;
import com.rockbite.tools.talos.runtime.values.NumericalValue;

public class InputModule extends Module {

    public static final int OUTPUT = 0;
    private NumericalValue outputValue;

    private int scopeKey;

    @Override
    protected void defineSlots() {
        outputValue = createOutputSlot(OUTPUT);
    }

    @Override
    public void processValues() {
        NumericalValue value = getScope().get(scopeKey);
        outputValue.set(value);
    }

    public void setInput(int scopeKey) {
        this.scopeKey = scopeKey;
    }

    public int getInput() {
        return this.scopeKey;
    }

    @Override
    public void write (Json json) {
        json.writeValue("scopeKey", getInput(), int.class);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        setInput(jsonData.getInt("scopeKey"));
    }
}
