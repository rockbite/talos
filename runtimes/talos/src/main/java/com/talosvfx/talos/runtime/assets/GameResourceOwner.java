package com.talosvfx.talos.runtime.assets;

import com.badlogic.gdx.utils.GdxRuntimeException;
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
        String talosIdentifier = readTalosIdentifier(jsonValue);
        RuntimeContext.TalosContext talosContext = RuntimeContext.getInstance().getTalosContext(talosIdentifier);
        if (talosContext == null) {
            throw new GdxRuntimeException("No asset repository found for " + talosIdentifier);
        }
        BaseAssetRepository baseAssetRepository = talosContext.getBaseAssetRepository();

        if (uuid == null) {
            GameAsset<U> asset = baseAssetRepository.getAssetForIdentifier(identifier, type);
            if (asset == null) {
                System.out.println("Asset not found for identifier " + identifier + " and type " + type);
            }
            return asset;
        } else {
            GameAsset<U> assetForUniqueIdentifier = baseAssetRepository.getAssetForUniqueIdentifier(uuid, type);
            if (assetForUniqueIdentifier == null || assetForUniqueIdentifier.isNonFound()) {
                if (identifier != null) {
                    GameAsset<U> asset = baseAssetRepository.getAssetForIdentifier(identifier, type);
                    if (asset == null) {
                        System.out.println("Asset not found even with identifier fallback");
                    } else {
                        return asset;
                    }
                }
                System.out.println("Asset not found for uuid " + uuid + " and type " + type);
            }
            return assetForUniqueIdentifier;
        }
    }

    static <U> GameAsset<U> readAssetForceType (Json json, JsonValue jsonValue, GameAssetType type) {
        String identifier = readGameResourceFromComponent(jsonValue);
        UUID uuid = readGameResourceUUIDFromComponent(jsonValue);
        String talosIdentifier = readTalosIdentifier(jsonValue);
        RuntimeContext.TalosContext talosContext = RuntimeContext.getInstance().getTalosContext(talosIdentifier);
        if (talosContext == null) {
            throw new GdxRuntimeException("No asset repository found for " + talosIdentifier);
        }
        BaseAssetRepository baseAssetRepository = talosContext.getBaseAssetRepository();

        if (uuid == null) {
            GameAsset<U> asset = baseAssetRepository.getAssetForIdentifier(identifier, type);
            if (asset == null) {
                System.out.println("Asset not found for identifier " + identifier + " and type " + type);
            }
            return asset;
        } else {
            GameAsset<U> assetForUniqueIdentifier = baseAssetRepository.getAssetForUniqueIdentifier(uuid, type);
            if (assetForUniqueIdentifier == null || assetForUniqueIdentifier.isNonFound()) {
                if (identifier != null) {
                    GameAsset<U> asset = baseAssetRepository.getAssetForIdentifier(identifier, type);
                    if (asset == null) {
                        System.out.println("Asset not found even with identifier type");
                    } else {
                        return asset;
                    }
                }
                System.out.println("Asset not found for uuid " + uuid + " and type " + type);
            }
            return assetForUniqueIdentifier;
        }
    }

    static GameAssetType readAssetType(Json json, JsonValue jsonValue) {
        GameAssetType type = json.readValue("type", GameAssetType.class, jsonValue);
        if(type == null) return GameAssetType.SPRITE;
        return type;
    }
    static String readTalosIdentifier (JsonValue value) {
        return value.getString("talosIdentifier", "default");
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

    void clearResource ();
}
