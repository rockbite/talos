package com.talosvfx.talos.editor.addons.scene.utils.propertyWrappers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.GameAssetType;
import com.talosvfx.talos.editor.addons.scene.logic.components.GameResourceOwner;


public class PropertyGameAssetWrapper extends PropertyWrapper<GameAsset<?>> implements GameResourceOwner {

    private transient GameAsset.GameAssetUpdateListener gameAssetUpdateListener = new GameAsset.GameAssetUpdateListener() {
        @Override
        public void onUpdate() {

        }
    };

    @Override
    public GameAsset<Texture> parseValueFromString(String value) {
        return null; // todo: this is important when we bring back the scripts
    }

    @Override
    public PropertyType getType() {
        return PropertyType.ASSET;
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);

        GameAsset<?> asset = GameResourceOwner.readAsset(json, jsonData);
        if (asset != null) {
            setGameAsset(asset);
        }
    }

    @Override
    public void write (Json json) {
        super.write(json);
        GameResourceOwner.writeGameAsset(json, this);
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
