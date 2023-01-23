package com.talosvfx.talos.editor.addons.scene.utils.propertyWrappers;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyType;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyGameObjectWrapper extends PropertyWrapper<GameObject> {


    private static final Logger logger = LoggerFactory.getLogger(PropertyGameObjectWrapper.class);

    private String objectGOUUID;
    private String defaultGOUuid;


    @Override
    public GameObject parseValueFromString (String value) {
        logger.info("Redo parse value from string");
        return null;

//        SceneEditorWorkspace instance = SceneEditorWorkspace.getInstance();
//        if (instance == null) {
//             not initialized yet, gotta go
//            return null;
//        }
//
//        if (value.isEmpty()) {
//            return null;
//        }
//
//        return instance.getGameObjectForUUID(value);
    }

    @Override
    public void setValue (GameObject value) {
        super.setValue(value);
        if (value == null) {
            objectGOUUID = null;
        } else {
            objectGOUUID = value.uuid.toString();
        }
    }

    @Override
    public void write (Json json) {
        super.write(json);
        json.writeValue("valueGO", objectGOUUID);
        json.writeValue("defaultGO", defaultGOUuid);
    }

    @Override
    public GameObject getValue () {
        logger.info("redo getVAlue");
        return null;
//        if (objectGOUUID == null) {
//            return null;
//        }
//
//        if (value == null) {
//            value = SceneEditorWorkspace.getInstance().getGameObjectForUUID(objectGOUUID);
//        }
//
//        return super.getValue();
    }

    @Override
    public void collectAttributes (Array<String> attributes) {
        super.collectAttributes(attributes);
        if (defaultValue != null) {
            defaultGOUuid = defaultValue.uuid.toString();
        }
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        objectGOUUID = jsonData.getString("valueGO");
        defaultGOUuid = jsonData.getString("defaultGO");
    }

    @Override
    public PropertyType getType() {
        // TODO: 12/23/2022 IMPLEMENT THIS TYPE
        return null;
    }
}
