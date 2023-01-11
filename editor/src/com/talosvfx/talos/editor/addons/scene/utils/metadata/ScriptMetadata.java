package com.talosvfx.talos.editor.addons.scene.utils.metadata;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.addons.scene.utils.AMetadata;
import com.talosvfx.talos.editor.addons.scene.utils.propertyWrappers.ScriptMetadataParser;
import com.talosvfx.talos.editor.addons.scene.utils.propertyWrappers.PropertyWrapper;

public class ScriptMetadata extends AMetadata {

    public Array<PropertyWrapper<?>> scriptPropertyWrappers;

    public ScriptMetadata () {
        super();
        scriptPropertyWrappers = new Array<>();
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        scriptPropertyWrappers.clear();
        JsonValue propertiesJson = jsonData.get("scriptProperties");
        if (propertiesJson != null) {
            for (JsonValue propertyJson : propertiesJson) {
                scriptPropertyWrappers.add(json.readValue(PropertyWrapper.class, propertyJson));
            }
        }
    }

    @Override
    public void write (Json json) {
        super.write(json);
        json.writeValue("scriptProperties", scriptPropertyWrappers);
    }

    @Override
    public void postProcessForHandle (FileHandle handle) {
        super.postProcessForHandle(handle);
        scriptPropertyWrappers.clear();
        ScriptMetadataParser scriptMetadataParser = new ScriptMetadataParser();
        scriptMetadataParser.processHandle(handle, this);
    }
}
