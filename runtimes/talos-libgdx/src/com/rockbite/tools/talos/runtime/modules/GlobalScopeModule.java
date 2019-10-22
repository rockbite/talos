package com.rockbite.tools.talos.runtime.modules;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.rockbite.tools.talos.runtime.values.NumericalValue;

public class GlobalScopeModule extends Module {

    public static final int OUTPUT = 0;
    NumericalValue output;
    private int key;

    @Override
    protected void defineSlots() {
        output = createOutputSlot(OUTPUT);
    }

    @Override
    public void processValues() {
        output.set(getScope().getDynamicValue(key));
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        key = jsonData.getInt("key", 0);
    }

    @Override
    public void write(Json json) {
        super.write(json);
        json.writeValue("key", key);
    }
}
