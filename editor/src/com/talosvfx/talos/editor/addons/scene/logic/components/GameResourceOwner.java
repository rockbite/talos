package com.talosvfx.talos.editor.addons.scene.logic.components;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.GameAssetType;

public interface GameResourceOwner<U> {

    GameAssetType getGameAssetType ();

    GameAsset<U> getGameResource ();

    void setGameAsset (GameAsset<U> gameAsset);

    static <U> void writeGameAsset (Json json, GameResourceOwner<U> owner) {
        GameAsset<U> gameResource = owner.getGameResource();
        if (gameResource != null) {
            json.writeValue("gameResource", gameResource.nameIdentifier);
            if (!gameResource.isBroken()) {
                json.writeValue("gameResourceExtension", gameResource.getRootRawAsset().handle.extension());
                json.writeValue("type", gameResource.type);
            }
        }
    }

    static<U> GameAsset<U> readAsset(Json json, JsonValue jsonValue) {
        String identifier = readGameResourceFromComponent(jsonValue);
        GameAssetType type = readAssetType(json, jsonValue);

        GameAsset<U> asset = AssetRepository.getInstance().getAssetForIdentifier(identifier, type);

        return asset;
    }

    static GameAssetType readAssetType(Json json, JsonValue jsonValue) {
        GameAssetType type = json.readValue("type", GameAssetType.class, jsonValue);
        if(type == null) return GameAssetType.SPRITE;
        return type;
    }

    static String readGameResourceFromComponent (JsonValue component) {
        return component.getString("gameResource", "broken");
    }

}
