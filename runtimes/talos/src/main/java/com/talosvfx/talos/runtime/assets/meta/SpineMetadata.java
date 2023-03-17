package com.talosvfx.talos.runtime.assets.meta;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.assets.AMetadata;


public class SpineMetadata extends AMetadata {

    public float pixelsPerUnit = DefaultConstants.defaultPixelPerUnitProvider.get();

    public String atlasPath;

    public SpineMetadata() {
        super();
    }


    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        pixelsPerUnit = jsonData.getFloat("pixelsPerUnit", DefaultConstants.defaultPixelPerUnitProvider.get());
    }

    @Override
    public void write (Json json) {
        super.write(json);
        json.writeValue("pixelsPerUnit", pixelsPerUnit);
    }

}
