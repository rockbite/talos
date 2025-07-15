package com.talosvfx.talos.runtime.scene.components;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.RuntimeContext;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.runtime.assets.GameResourceOwner;
import com.talosvfx.talos.runtime.assets.meta.ScriptMetadata;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyWrapper;
import lombok.Getter;

import java.util.UUID;

public class ScriptComponent extends AComponent implements Json.Serializable, GameResourceOwner<String> {

    @Getter
    private GameAsset<String> scriptResource;

    @Getter
    private Array<PropertyWrapper<?>> scriptProperties = new Array<>();

    @Override
    public GameAssetType getGameAssetType () {
        return GameAssetType.SCRIPT;
    }

    @Override
    public GameAsset<String> getGameResource () {
        return scriptResource;
    }

    @Override
    public void setGameAsset (GameAsset<String> gameAsset) {
        this.scriptResource = gameAsset;
        scriptProperties.clear();
        importScriptPropertiesFromMeta(false);
    }

    @Override
    public void clearResource () {
        if (scriptResource != null) {
            scriptResource = null;
        }
    }

    @Override
    public void write (Json json) {
        GameResourceOwner.writeGameAsset(json, this);
        json.writeValue("properties", scriptProperties);

    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        GameAsset<String> objectGameAsset = GameResourceOwner.readAsset(json, jsonData);
        setGameAsset(objectGameAsset);

        scriptProperties.clear();
        JsonValue propertiesJson = jsonData.get("properties");
        if (propertiesJson != null) {
            for (JsonValue propertyJson : propertiesJson) {
                scriptProperties.add(json.readValue(PropertyWrapper.class, propertyJson));
            }
        }
    }



    public void importScriptPropertiesFromMeta (boolean tryToMerge) {
        Array<PropertyWrapper<?>> copyWrappers = new Array<>();
        copyWrappers.addAll(scriptProperties);

        scriptProperties.clear();
        if (getGameResource() != null) {
            ScriptMetadata metadata = ((ScriptMetadata) getGameResource().getRootRawAsset().metaData);
            for (PropertyWrapper<?> propertyWrapper : metadata.scriptPropertyWrappers) {
                scriptProperties.add(propertyWrapper.clone());
            }
        }

        if (tryToMerge) {
            for (PropertyWrapper copyWrapper : copyWrappers) {
                for (PropertyWrapper scriptProperty : scriptProperties) {
                    if (copyWrapper.propertyName.equals(scriptProperty.propertyName) && copyWrapper.getClass().equals(scriptProperty.getClass())) {
                        scriptProperty.setValue(copyWrapper.getValue());
                        break;
                    }
                }
            }

            for (PropertyWrapper<?> scriptProperty : scriptProperties) {
                if (scriptProperty.getValue() == null) {
                    scriptProperty.setDefault();
                }
            }

        } else {
            for (PropertyWrapper<?> scriptProperty : scriptProperties) {
                scriptProperty.setDefault();
            }
        }
    }

    @Override
    public boolean allowsMultipleOfTypeOnGameObject () {
        return true;
    }
}
