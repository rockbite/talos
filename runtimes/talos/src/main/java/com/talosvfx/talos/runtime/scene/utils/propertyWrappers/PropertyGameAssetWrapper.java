package com.talosvfx.talos.runtime.scene.utils.propertyWrappers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasSprite;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.runtime.assets.GameResourceOwner;

public class PropertyGameAssetWrapper extends PropertyWrapper<GameAsset<?>> implements GameResourceOwner {

    private transient GameAsset.GameAssetUpdateListener gameAssetUpdateListener = new GameAsset.GameAssetUpdateListener() {
        @Override
        public void onUpdate() {

        }
    };

    @Override
    public GameAsset<AtlasSprite> parseValueFromString(String value) {
        return null; // todo: this is important when we bring back the scripts
    }

    @Override
    public PropertyType getType() {
        return PropertyType.ASSET;
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);

        JsonValue value = jsonData.get("value");
        JsonValue def = jsonData.get("default");

        defaultValue = GameResourceOwner.readAsset(json, def);

        GameAsset<?> asset = GameResourceOwner.readAsset(json, value);
        if (asset != null) {
            if(asset.isBroken()) {
                asset = defaultValue;
            }
            setGameAsset(asset);
        }
    }

    @Override
    public void write (Json json) {
        super.write(json);
        json.writeObjectStart("value");
        GameResourceOwner.writeGameAsset(json, this);
        json.writeObjectEnd();
        json.writeObjectStart("default");
        GameResourceOwner.writeGameAsset("gameResource", json, defaultValue);
        json.writeObjectEnd();
    }

    @Override
    public GameAssetType getGameAssetType() {
        return value.type;
    }

    @Override
    public GameAsset getGameResource() {
        return value;
    }

    @Override
    public void setGameAsset(GameAsset newGameAsset) {
        if (this.value != null) {
            //Remove from old game asset, it might be the same, but it may also have changed
            this.value.listeners.removeValue(gameAssetUpdateListener, true);
        }

        if(defaultValue == null && !newGameAsset.isBroken()){
            defaultValue = newGameAsset;
        }

        this.value = newGameAsset;
        this.value.listeners.add(gameAssetUpdateListener);

        gameAssetUpdateListener.onUpdate();
    }
}
