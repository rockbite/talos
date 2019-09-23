package com.rockbite.tools.talos.runtime.modules;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.rockbite.tools.talos.runtime.values.EmConfigValue;

public class EmConfigModule extends Module {

    public static final int OUTPUT = 0;

    private EmConfigValue userValue = new EmConfigValue();
    private EmConfigValue outputValue;

    @Override
    public void init () {
        super.init();

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


    @Override
    public void write(Json json) {
        json.writeValue("additive", getUserValue().additive);
        json.writeValue("attached", getUserValue().attached);
        json.writeValue("continuous", getUserValue().continuous);
        json.writeValue("aligned", getUserValue().aligned);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        getUserValue().additive = jsonData.getBoolean("additive");
        getUserValue().attached = jsonData.getBoolean("attached");
        getUserValue().continuous = jsonData.getBoolean("continuous");
        getUserValue().aligned = jsonData.getBoolean("aligned");
    }
}
