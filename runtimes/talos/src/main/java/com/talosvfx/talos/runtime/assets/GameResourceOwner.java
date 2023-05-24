package com.talosvfx.talos.runtime.assets;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.RuntimeContext;

import java.util.UUID;

public interface GameResourceOwner<U> {

    GameAssetType getGameAssetType ();

    GameAsset<U> getGameResource ();

    void setGameAsset (GameAsset<U> gameAsset);

    static <U> void writeGameAsset (String name, Json json, GameAsset<U> gameResource) {
        if (gameResource != null) {
            json.writeValue(name, gameResource.nameIdentifier);
            if (!gameResource.isBroken()) {
                json.writeValue("gameResourceExtension", gameResource.getRootRawAsset().handle.extension());
                json.writeValue("type", gameResource.type);
                json.writeValue("gameResourceUUID", gameResource.getRootRawAsset().metaData.uuid.toString());
            } else {
                json.writeValue("gameResourceExtension", "png");
                json.writeValue("type", GameAssetType.SPRITE);
                json.writeValue("gameResourceUUID", RuntimeAssetRepository.missingUUID.toString());
            }
        }
    }

    static <U> void writeGameAsset (String name, Json json, GameResourceOwner<U> owner) {
        writeGameAsset(name, json, owner.getGameResource());
    }

    static <U> void writeGameAsset (Json json, GameResourceOwner<U> owner) {
        writeGameAsset("gameResource", json, owner);
    }

    static <U> GameAsset<U> readAsset (Json json, JsonValue jsonValue) {
        String identifier = readGameResourceFromComponent(jsonValue);
        GameAssetType type = readAssetType(json, jsonValue);
        UUID uuid = readGameResourceUUIDFromComponent(jsonValue);

        if (uuid == null) {
            return RuntimeContext.getInstance().AssetRepository.getAssetForIdentifier(identifier, type);
        } else {
            return RuntimeContext.getInstance().AssetRepository.getAssetForUniqueIdentifier(uuid, type);
        }
    }

    static GameAssetType readAssetType(Json json, JsonValue jsonValue) {
        GameAssetType type = json.readValue("type", GameAssetType.class, jsonValue);
        if(type == null) return GameAssetType.SPRITE;
        return type;
    }

    static String readGameResourceFromComponent (JsonValue component) {
        return component.getString("gameResource", "broken");
    }

    static UUID readGameResourceUUIDFromComponent (JsonValue jsonValue) {
        String uuid = jsonValue.getString("gameResourceUUID", null);
        if (uuid == null) {
            return null;
        } else {
            return UUID.fromString(uuid);
        }
    }
}
