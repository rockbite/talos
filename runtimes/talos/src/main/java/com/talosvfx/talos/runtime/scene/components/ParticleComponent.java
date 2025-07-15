package com.talosvfx.talos.runtime.scene.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.RuntimeContext;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.runtime.assets.GameResourceOwner;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.vfx.ParticleEffectInstance;
import com.talosvfx.talos.runtime.vfx.serialization.BaseVFXProjectData;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class ParticleComponent<T extends BaseVFXProjectData> extends RendererComponent implements GameResourceOwner<T> {
    private transient GameAsset<T> defaultGameAsset;

    @Getter@Setter
    private transient ParticleEffectInstance effectRef;

    public GameAsset<T> gameAsset;

    @Override
    public void read (Json json, JsonValue jsonData) {
        GameAsset<T> newGameAsset = GameResourceOwner.readAsset(json, jsonData);
        setGameAsset(newGameAsset);

        super.read(json, jsonData);
    }

    @Override
    public void write (Json json) {
        GameResourceOwner.writeGameAsset(json, this);
        super.write(json);
    }

    GameAsset.GameAssetUpdateListener gameAssetUpdateListener = new GameAsset.GameAssetUpdateListener() {
        @Override
        public void onUpdate () {
            if (gameAsset.isBroken()) {

            } else {
                //Its ok
            }
        }
    };



    @Override
    public GameAssetType getGameAssetType () {
        return GameAssetType.VFX;
    }

    @Override
    public GameAsset<T> getGameResource () {
        return this.gameAsset;
    }

    @Override
    public void setGameAsset (GameAsset<T> newGameAsset) {
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
    }

    @Override
    public void clearResource () {
        if (gameAsset != null) {
            gameAsset.listeners.removeValue(gameAssetUpdateListener, true);
            gameAsset = null;
        }
        effectRef = null;
        defaultGameAsset = null;
    }
    Vector2 vec = new Vector2();
    @Override
    public void minMaxBounds (GameObject ownerEntity, BoundingBox boundingBox) {
        TransformComponent transformComponent = ownerEntity.getComponent(TransformComponent.class);
        if (transformComponent != null) {
            vec.set(0, 0);
            transformComponent.localToWorld(ownerEntity, vec);

            float width = transformComponent.scale.x * 1;
            float height = transformComponent.scale.y * 1;

            boundingBox.ext(vec.x - width/2, vec.y - height/2, 0);
            boundingBox.ext(vec.x + width/2, vec.y + height/2, 0);
        }
    }

    @Override
    public void reset() {
        super.reset();
        if(defaultGameAsset!=null) {
            setGameAsset(defaultGameAsset);
        }
    }
}
