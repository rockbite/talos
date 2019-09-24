package com.rockbite.tools.talos.runtime.modules;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.rockbite.tools.talos.runtime.render.TextureRegionDrawable;
import com.rockbite.tools.talos.runtime.values.DrawableValue;

public class TextureModule extends Module {

    public static final int OUTPUT = 0;

    private DrawableValue userDrawable;
    private DrawableValue outputValue;

    @Override
    protected void defineSlots() {
        outputValue = (DrawableValue) createOutputSlot(OUTPUT, new DrawableValue());
        userDrawable = new DrawableValue();
        userDrawable.setEmpty(true);
    }

    @Override
    public void processValues() {
        outputValue.set(userDrawable);
    }

    public void setRegion(TextureRegion region) {
        userDrawable.setDrawable(new TextureRegionDrawable(region));
    }

    @Override
    public void write (Json json) {
        //todo texture serialization
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        //todo texture serialization
    }


}
