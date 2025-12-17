package com.talosvfx.talos.runtime.assets.meta;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.assets.AMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlipBookMetadata extends AMetadata {

    private static final Logger logger = LoggerFactory.getLogger(FlipBookMetadata.class);

    public FlipBookMetadata () {
        super();
    }

    @Override
    public void write (Json json) {
        super.write(json);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
    }

}
