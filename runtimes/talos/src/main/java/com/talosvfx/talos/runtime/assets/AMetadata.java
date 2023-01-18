package com.talosvfx.talos.runtime.assets;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import java.util.UUID;

public abstract class AMetadata implements Json.Serializable {


    public RawAsset link;

    public UUID uuid;

    public AMetadata () {
        uuid = UUID.randomUUID();
    }

    public void setLinkRawAsset (RawAsset link) {
        this.link = link;
    }




    public void postProcessForHandle (FileHandle handle) {
    }


    @Override
    public void write (Json json) {
        json.writeValue("uuid", uuid.toString());
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        uuid = UUID.fromString(jsonData.getString("uuid"));
    }


}
