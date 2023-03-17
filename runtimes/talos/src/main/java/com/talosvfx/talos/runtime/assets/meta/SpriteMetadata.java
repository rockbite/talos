package com.talosvfx.talos.runtime.assets.meta;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.assets.AMetadata;
import com.talosvfx.talos.runtime.utils.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpriteMetadata extends AMetadata {

    private static final Logger logger = LoggerFactory.getLogger(SpriteMetadata.class);

    public int[] borderData = {0, 0, 0, 0};

    public float pixelsPerUnit = DefaultConstants.defaultPixelPerUnitProvider.get();

    public Texture.TextureFilter minFilter = Texture.TextureFilter.Nearest;
    public Texture.TextureFilter magFilter = Texture.TextureFilter.Nearest;

    public SpriteMetadata() {
        super();
    }

    @Override
    public void write (Json json) {
        super.write(json);
        json.writeValue("borderData", borderData);
        json.writeValue("pixelsPerUnit", pixelsPerUnit);
        json.writeValue("minFilter", minFilter);
        json.writeValue("minFilter", magFilter);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        JsonValue borderDataJsonValue = jsonData.get("borderData");
        if (borderDataJsonValue != null) {
            borderData = json.readValue(int[].class, borderDataJsonValue);
        }
        pixelsPerUnit = jsonData.getFloat("pixelsPerUnit", DefaultConstants.defaultPixelPerUnitProvider.get());
        minFilter = json.readValue("minFilter", Texture.TextureFilter.class, Texture.TextureFilter.Nearest, jsonData);
        magFilter = json.readValue("magFilter", Texture.TextureFilter.class, Texture.TextureFilter.Nearest, jsonData);
    }

    public boolean isSlice () {
        for (int i = 0; i < 4; i++) {
            boolean isNonZeroBorderData = borderData[i] != 0;
            if (isNonZeroBorderData) return true;
        }
        return false;
    }
}
