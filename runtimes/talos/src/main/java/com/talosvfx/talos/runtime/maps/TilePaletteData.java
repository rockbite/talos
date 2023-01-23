package com.talosvfx.talos.runtime.maps;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;
import com.talosvfx.talos.runtime.RuntimeContext;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.components.TileDataComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class TilePaletteData implements Json.Serializable {

    private static final Logger logger = LoggerFactory.getLogger(TilePaletteData.class);
    public ObjectMap<UUID, GameAsset<?>> references;


    //Working not for serializing
    public transient OrderedMap<GameAsset<?>, GameObject> gameObjects;
    public transient GameObject rootDummy;

    public void addSprite (GameAsset<?> gameAsset, Vector2 position) {
        GridPosition gridPosition = new GridPosition(MathUtils.round(position.x), MathUtils.round(position.y));
        StaticTile staticTile = new StaticTile(gameAsset, gridPosition);
        TileGameObjectProxy tileGameObjectProxy = new TileGameObjectProxy();
        tileGameObjectProxy.staticTile = staticTile;
        TileDataComponent component = new TileDataComponent();
        component.getParentTiles().add(staticTile.getGridPosition());
        tileGameObjectProxy.addComponent(component);
        gameObjects.put(gameAsset, tileGameObjectProxy);
    }

    public void addEntity (GameAsset<?> gameAsset, GameObject gameObject) {
        gameObjects.put(gameAsset, gameObject);
        rootDummy.addGameObject(gameObject);
    }

    public GameObject addEntity (GameAsset<?> gameAsset) {
        //Lets create an entity from the asset
        logger.info("Shouldm probably double triple check this commented code if bugs from asset importer happen");

//        AssetImporter.fromDirectoryView = true; //tom is very naughty dont be like tom
//        GameObject tempParent = new GameObject();
//        boolean success = AssetImporter.createAssetInstance(gameAsset, tempParent) != null;
//        if (tempParent.getGameObjects() == null || tempParent.getGameObjects().size == 0) {
//            success = false;
//        }
//        AssetImporter.fromDirectoryView = false;

//        if (success) {
//            GameObject first = tempParent.getGameObjects().first();
//            first.parent = null;
//            if (!first.hasComponent(TilePaletteData.class)) {
//                first.addComponent(new TileDataComponent());
//            }
//            gameObjects.put(gameAsset, first);
//            rootDummy.addGameObject(first);
//            return first;
//        }
        return null;
    }

    public void removeEntity (GameAsset<?> gameAsset) {
        GameObject gameObject = gameObjects.remove(gameAsset);
        rootDummy.removeObject(gameObject);

        logger.info("redo remove gizmos");
//        SceneEditorWorkspace.getInstance().removeGizmos(gameObject);
    }

    public enum TileOrEntity {
        TILE,
        ENTITY
    }

    public TilePaletteData () {
        references = new ObjectMap<>();

        gameObjects = new OrderedMap<>();
    }

    @Override
    public void write(Json json) {
        json.writeArrayStart("references");
        for (UUID uuid: references.keys()) {
            GameAsset<?> reference = references.get(uuid);

            json.writeObjectStart();
            json.writeValue("gameIdentifier", reference.nameIdentifier);
            json.writeValue("type", reference.type);
            TileOrEntity tileOrEntity = getTileOrEntity(reference);
            json.writeValue("tileOrEntity", tileOrEntity);

            if (tileOrEntity == TileOrEntity.ENTITY) {
                GameObject gameObject = gameObjects.get(reference);
                json.writeValue("entity", gameObject);
            } else if (tileOrEntity == TileOrEntity.TILE) {
                json.writeObjectStart("position");

                TileGameObjectProxy gameObject = (TileGameObjectProxy)gameObjects.get(reference);
                GridPosition position = gameObject.staticTile.getGridPosition();

                json.writeValue("x", position.getIntX());
                json.writeValue("y", position.getIntY());
                json.writeObjectEnd();
            }

            json.writeObjectEnd();
        }
        json.writeArrayEnd();
    }

    private TileOrEntity getTileOrEntity (GameAsset<?> reference) {
        if (gameObjects.containsKey(reference) && !(gameObjects.get(reference) instanceof TileGameObjectProxy)) {
            return TileOrEntity.ENTITY;
        }
        return TileOrEntity.TILE;
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        rootDummy = new GameObject();

        JsonValue references = jsonData.get("references");
        for (JsonValue reference : references) {
            GameAsset<Object> assetForIdentifier;

            String identifier = reference.getString("gameIdentifier");
            JsonValue typeVal = reference.get("type");
            GameAssetType type = json.readValue(GameAssetType.class, typeVal);
            assetForIdentifier = RuntimeContext.getInstance().AssetRepository.getAssetForIdentifier(identifier, type);

            TileOrEntity tileOrEntity = json.readValue(TileOrEntity.class, reference.get("tileOrEntity"));

            UUID uuid = assetForIdentifier.getRootRawAsset().metaData.uuid;

            if (tileOrEntity == TileOrEntity.ENTITY) {
                GameObject gameObject = json.readValue(GameObject.class, reference.get("entity"));
                addEntity(assetForIdentifier, gameObject);

            } else if (tileOrEntity == TileOrEntity.TILE) {
                JsonValue posVal = reference.get("position");
                float x = posVal.get(0).asFloat();
                float y = posVal.get(1).asFloat();
                float[] position = new float[]{x, y};

                addSprite(assetForIdentifier, new Vector2(position[0], position[1]));
            }
            this.references.put(uuid, assetForIdentifier);


            if (assetForIdentifier == null) {
                System.out.println(type + " with identifier " + identifier + " is not found.");
            }
        }
    }
}
