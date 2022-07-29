package com.talosvfx.talos.editor.addons.scene.logic;

import com.badlogic.gdx.utils.*;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.GameAssetType;
import com.talosvfx.talos.editor.addons.scene.maps.GridPosition;
import com.talosvfx.talos.editor.addons.scene.maps.StaticTile;
import com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter;

import java.util.UUID;

public class TilePaletteData implements Json.Serializable{
    public ObjectMap<UUID, GameAsset<?>> references;
    public ObjectMap<UUID, float[]> positions;


    //Working not for serializing
    public transient ObjectMap<GameAsset<?>, StaticTile> staticTiles;
    public transient ObjectMap<GameAsset<?>, GameObject> gameObjects;
    public transient Array<GameAsset<?>> selectedGameAssets;

    public void addSprite (GameAsset<?> gameAsset) {
        GridPosition gridPosition = new GridPosition(0, 0);
        staticTiles.put(gameAsset, new StaticTile(gameAsset, gridPosition));
    }

    public void addEntity (GameAsset<?> gameAsset) {
        //Lets create an entity from the asset
        AssetImporter.fromDirectoryView = true; //tom is very naughty dont be like tom
        GameObject tempParent = new GameObject();
        boolean success = AssetImporter.createAssetInstance(gameAsset, tempParent);
        if (tempParent.getGameObjects() == null || tempParent.getGameObjects().size == 0) {
            success = false;
        }
        AssetImporter.fromDirectoryView = false;

        if (success) {
            gameObjects.put(gameAsset, tempParent.getGameObjects().first());
        }
    }

    public enum TileOrEntity {
        TILE,
        ENTITY
    }

    public TilePaletteData () {
        references = new ObjectMap<>();
        positions = new ObjectMap<>();

        staticTiles = new ObjectMap<>();
        gameObjects = new ObjectMap<>();
        selectedGameAssets = new Array<>();
    }

    @Override
    public void write(Json json) {
        json.writeArrayStart("references");
        for (UUID uuid: references.keys()) {
            GameAsset<?> reference = references.get(uuid);
            float[] position = positions.get(uuid);

            json.writeObjectStart();
            json.writeValue("gameIdentifier", reference.nameIdentifier);
            json.writeValue("type", reference.type);
            json.writeValue("tileOrEntity", getTileOrEntity(reference));

                json.writeObjectStart("position");
                json.writeValue("x", position[0]);
                json.writeValue("y", position[1]);
                json.writeObjectEnd();

            json.writeObjectEnd();
        }
        json.writeArrayEnd();
    }

    private TileOrEntity getTileOrEntity (GameAsset<?> reference) {
        if (gameObjects.containsKey(reference)) {
            return TileOrEntity.ENTITY;
        }
        return TileOrEntity.TILE;
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        JsonValue references = jsonData.get("references");
        for (JsonValue reference : references) {
            GameAsset<Object> assetForIdentifier;
            float[] position;

            String identifier = reference.getString("gameIdentifier");
            JsonValue typeVal = reference.get("type");
            GameAssetType type = json.readValue(GameAssetType.class, typeVal);
            assetForIdentifier = AssetRepository.getInstance().getAssetForIdentifier(identifier, type);

            TileOrEntity tileOrEntity = json.readValue(TileOrEntity.class, reference.get("tileOrEntity"));

            JsonValue posVal = reference.get("position");
            float x = posVal.get(0).asFloat();
            float y = posVal.get(1).asFloat();
            position = new float[]{x, y};

            UUID uuid = assetForIdentifier.getRootRawAsset().metaData.uuid;
            if (assetForIdentifier == null) {
                System.out.println(type + " with identifier " + identifier + " is not found.");
            } else {
                // TODO: add type restrictions to references' type, e. g. you cannot have palette reference inside of a palette
                this.references.put(uuid, assetForIdentifier);
                this.positions.put(uuid, position);
            }

            switch (tileOrEntity) {
            case TILE:
                addSprite(assetForIdentifier);
                break;
            case ENTITY:
                addEntity(assetForIdentifier);
                break;
            }
        }
    }
}
