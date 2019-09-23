package com.rockbite.tools.talos.runtime.modules;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.rockbite.tools.talos.runtime.values.NumericalValue;

public class StaticValueModule extends Module {

    public static final int OUTPUT = 0;

    private NumericalValue staticValue;
    private NumericalValue outputValue;

    @Override
    protected void defineSlots() {
        outputValue = createOutputSlot(OUTPUT);

        staticValue = new NumericalValue();
        staticValue.set(1f);
    }

    @Override
    public void processValues() {
        outputValue.set(staticValue);
    }

    public void setStaticValue(float val) {
        staticValue.set(val);
    }

    public float getStaticValue() {
        return staticValue.getFloat();
    }

    public NumericalValue getOutputValue() {
        return outputValue;
    }

    @Override
    public void write (Json json) {
        json.writeValue("value", getStaticValue());
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        setStaticValue(jsonData.getFloat("value"));
    }

}
