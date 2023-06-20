package com.talosvfx.talos.runtime.scene.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasSprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.RuntimeContext;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.runtime.assets.GameResourceOwner;
import com.talosvfx.talos.runtime.graphics.NineSlice;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.IColorHolder;
import com.talosvfx.talos.runtime.scene.ISizableComponent;
import com.talosvfx.talos.runtime.scene.ValueProperty;

import java.util.UUID;

import static com.badlogic.gdx.graphics.g2d.TextureAtlas.*;

public class SpriteRendererComponent extends RendererComponent implements GameResourceOwner<AtlasSprite>, ISizableComponent, IColorHolder {

    public transient GameAsset<AtlasSprite> defaultGameAsset;
    public GameAsset<AtlasSprite> gameAsset;

    public Color color = new Color(Color.WHITE);
    public transient Color finalColor = new Color();
    public boolean shouldInheritParentColor = true;
    public boolean flipX;
    public boolean flipY;
    private boolean fixAspectRatio = true;
    public RenderMode renderMode = RenderMode.simple;
    public NineSlice.RenderMode sliceMode = NineSlice.RenderMode.Simple;

    @ValueProperty(prefix = {"W", "H"})
    public Vector2 size = new Vector2(1, 1);

    @ValueProperty(prefix = {"W", "H"}, min = 0.05f)
    public Vector2 tileSize = new Vector2(1, 1);

    @Override
    public GameAssetType getGameAssetType () {
        return GameAssetType.SPRITE;
    }

    @Override
    public GameAsset<AtlasSprite> getGameResource () {
        return gameAsset;
    }

    @Override
    public void setGameAsset (GameAsset<AtlasSprite> newGameAsset) {
        if (this.gameAsset != null) {
            //Remove from old game asset, it might be the same, but it may also have changed
            this.gameAsset.listeners.removeValue(gameAssetUpdateListener, true);
        }

        if(defaultGameAsset == null && !newGameAsset.isBroken()){
            defaultGameAsset = newGameAsset;
        }

        this.gameAsset = newGameAsset;
        this.gameAsset.listeners.add(gameAssetUpdateListener);

        gameAssetUpdateListener.onUpdate();

        if (fixAspectRatio) {
            final AtlasSprite texture = this.gameAsset.getResource();

            if (texture == null) return;
            final float aspect = texture.getRegionHeight() * 1f / texture.getRegionWidth();
            size.y = size.x * aspect;
        }
    }

    public enum RenderMode {
        simple,
        sliced,
        tiled
    }


    transient GameAsset.GameAssetUpdateListener gameAssetUpdateListener = new GameAsset.GameAssetUpdateListener() {
        @Override
        public void onUpdate () {
            if (gameAsset.isBroken()) {
            } else {
            }
        }
    };

    private void loadTextureFromIdentifier (String gameResourceIdentifier) {
        GameAsset<AtlasSprite> assetForIdentifier = RuntimeContext.getInstance().AssetRepository.getAssetForIdentifier(gameResourceIdentifier, GameAssetType.SPRITE);
        setGameAsset(assetForIdentifier);
    }

    private void loadTextureFromUniqueIdentifier (UUID gameResourceIdentifier) {
        GameAsset<AtlasSprite> assetForUniqueIdentifier = RuntimeContext.getInstance().AssetRepository.getAssetForUniqueIdentifier(gameResourceIdentifier, GameAssetType.SPRITE);
        setGameAsset(assetForUniqueIdentifier);
    }

    @Override
    public void write (Json json) {
        GameResourceOwner.writeGameAsset(json, this);

        json.writeValue("color", color);
        json.writeValue("shouldInheritParentColor", shouldInheritParentColor);
        json.writeValue("flipX", flipX);
        json.writeValue("flipY", flipY);
        json.writeValue("fixAspectRatio", fixAspectRatio);
        json.writeValue("renderMode", renderMode);
        json.writeValue("sliceMode", sliceMode);
        json.writeValue("size", size);
        json.writeValue("tileSize", tileSize);

        super.write(json);

    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        UUID gameResourceUUID = GameResourceOwner.readGameResourceUUIDFromComponent(jsonData);
        if (gameResourceUUID == null) {
            String gameResourceIdentifier = GameResourceOwner.readGameResourceFromComponent(jsonData);
            loadTextureFromIdentifier(gameResourceIdentifier);
        } else {
            loadTextureFromUniqueIdentifier(gameResourceUUID);
            if (gameAsset.isBroken()) {
                //fallback
                String gameResourceIdentifier = GameResourceOwner.readGameResourceFromComponent(jsonData);
                loadTextureFromIdentifier(gameResourceIdentifier);
            }
        }

        color = json.readValue(Color.class, jsonData.get("color"));
        shouldInheritParentColor = jsonData.getBoolean("shouldInheritParentColor", true);
        if(color == null) color = new Color(Color.WHITE);

        flipX = jsonData.getBoolean("flipX", false);
        flipY = jsonData.getBoolean("flipY", false);
        fixAspectRatio = jsonData.getBoolean("fixAspectRatio", fixAspectRatio);
        renderMode = json.readValue(RenderMode.class, jsonData.get("renderMode"));
        sliceMode = json.readValue(NineSlice.RenderMode.class, jsonData.get("sliceMode"));
        JsonValue size = jsonData.get("size");
        if (size != null) {
            this.size = json.readValue(Vector2.class, size);
        }
        JsonValue tileSize = jsonData.get("tileSize");
        if (tileSize != null) {
            this.tileSize = json.readValue(Vector2.class, tileSize);
        }

        if(renderMode == null) renderMode = RenderMode.simple;
        if(sliceMode == null) sliceMode = NineSlice.RenderMode.Simple;

        super.read(json, jsonData);
    }

    Vector2 vec = new Vector2();
    @Override
    public void minMaxBounds (GameObject ownerEntity, BoundingBox boundingBox) {
        TransformComponent transformComponent = ownerEntity.getComponent(TransformComponent.class);
        if (transformComponent != null) {
            vec.set(0, 0);
            transformComponent.localToWorld(ownerEntity, vec);

            float width = transformComponent.scale.x * size.x;
            float height = transformComponent.scale.y * size.y;

            boundingBox.ext(-width/2, -height/2, 0);
            boundingBox.ext(+width/2, +height/2, 0);
        }
    }

    @Override
    public void reset() {
        super.reset();
        size.set(1, 1);
        color.set(Color.WHITE);
        flipX = false;
        flipY = false;
        fixAspectRatio = true;
        renderMode = RenderMode.simple;
        sliceMode = NineSlice.RenderMode.Simple;
        if (defaultGameAsset != null) {
            setGameAsset(defaultGameAsset);
        }
    }

    @Override
    public float getWidth() {
        return size.x;
    }

    @Override
    public float getHeight() {
        return size.y;
    }

    @Override
    public void setWidth(float width) {
        size.x = width;
    }

    @Override
    public void setHeight(float height) {
        size.y = height;
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public Color getFinalColor() {
        return finalColor;
    }

    @Override
    public boolean shouldInheritParentColor() {
        return shouldInheritParentColor;
    }

    public boolean shouldFixAspectRatio(boolean getRawValue) {
        if (getRawValue) {
            return fixAspectRatio;
        } else {
            if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                return !fixAspectRatio;
            } else {
                return fixAspectRatio;
            }
        }
    }
}
