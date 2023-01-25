package com.talosvfx.talos.runtime.scene.components;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.RuntimeContext;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.runtime.assets.GameResourceOwner;
import com.talosvfx.talos.runtime.scene.ValueProperty;

import java.util.UUID;
import java.util.function.Supplier;

public class PaintSurfaceComponent extends AComponent implements GameResourceOwner<Texture>, Json.Serializable {

    public GameAsset<Texture> gameAsset;

    @ValueProperty(prefix = {"W", "H"})
    public Vector2 size = new Vector2(1, 1);

    @ValueProperty(min = 0, max = 1, step=0.01f, progress = true)
    public float overlay = 0.5f;

    @ValueProperty
    public boolean redChannel = true;

    @ValueProperty
    public boolean greenChannel = true;

    @ValueProperty
    public boolean blueChannel = true;

    @ValueProperty
    public boolean alphaChannel = true;

    transient GameAsset.GameAssetUpdateListener gameAssetUpdateListener = new GameAsset.GameAssetUpdateListener() {
        @Override
        public void onUpdate () {
            if (gameAsset.isBroken()) {
            } else {
            }
        }
    };


    @Override
    public GameAssetType getGameAssetType () {
        return GameAssetType.SPRITE;
    }


    @Override
    public GameAsset<Texture> getGameResource () {
        return gameAsset;
    }

    @Override
    public void setGameAsset (GameAsset<Texture> newGameAsset) {
        if (this.gameAsset != null) {
            //Remove from old game asset, it might be the same, but it may also have changed
            this.gameAsset.listeners.removeValue(gameAssetUpdateListener, true);
        }

        this.gameAsset = newGameAsset;
        this.gameAsset.listeners.add(gameAssetUpdateListener);

        gameAssetUpdateListener.onUpdate();

    }

    @Override
    public void write(Json json) {
        GameResourceOwner.writeGameAsset(json, this);
        json.writeValue("overlay", overlay);
        json.writeValue("size", size, Vector2.class);
    }

    public void saveOnFile () {
        GameAsset<Texture> gameResource = getGameResource();
        FileHandle handle = gameResource.getRootRawAsset().handle;

        TextureData textureData = gameResource.getResource().getTextureData();
        if (!textureData.isPrepared()) {
            textureData.prepare();
        }
        PixmapIO.writePNG(handle, textureData.consumePixmap());
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        UUID gameResourceUUID = GameResourceOwner.readGameResourceUUIDFromComponent(jsonData);
        if (gameResourceUUID == null) {
            String gameResourceIdentifier = GameResourceOwner.readGameResourceFromComponent(jsonData);
            loadTextureFromIdentifier(gameResourceIdentifier);
        } else {
            loadTextureFromUniqueIdentifier(gameResourceUUID);
        }

        overlay = jsonData.getFloat("overlay", 0.5f);
        size = json.readValue( "size", Vector2.class, jsonData);
    }

    private void loadTextureFromIdentifier (String gameResourceIdentifier) {
        GameAsset<Texture> assetForIdentifier = RuntimeContext.getInstance().AssetRepository.getAssetForIdentifier(gameResourceIdentifier, GameAssetType.SPRITE);
        setGameAsset(assetForIdentifier);
    }

    private void loadTextureFromUniqueIdentifier (UUID gameResourceUUID) {
        GameAsset<Texture> assetForUniqueIdentifier = RuntimeContext.getInstance().AssetRepository.getAssetForUniqueIdentifier(gameResourceUUID, GameAssetType.SPRITE);
        setGameAsset(assetForUniqueIdentifier);
    }
}
