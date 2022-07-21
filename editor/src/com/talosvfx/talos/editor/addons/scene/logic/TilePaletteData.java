package com.talosvfx.talos.editor.addons.scene.logic;

import com.badlogic.gdx.utils.*;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.GameAssetType;

import java.util.UUID;

public class TilePaletteData implements Json.Serializable{
    public ObjectMap<UUID, GameAsset<?>> references;
    public ObjectMap<UUID, float[]> positions;

    public TilePaletteData () {
        references = new ObjectMap<>();
        positions = new ObjectMap<>();
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

                json.writeObjectStart("position");
                json.writeValue("x", position[0]);
                json.writeValue("y", position[1]);
                json.writeObjectEnd();

            json.writeObjectEnd();
        }
        json.writeArrayEnd();
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        JsonValue references = jsonData.get("references");
        for (JsonValue reference : references) {
            String identifier = reference.getString("gameIdentifier");
            JsonValue type1 = reference.get("type");
            GameAssetType type = json.readValue(GameAssetType.class, type1);

            GameAsset<Object> assetForIdentifier = AssetRepository.getInstance().getAssetForIdentifier(identifier, type);
            UUID uuid = assetForIdentifier.getRootRawAsset().metaData.uuid;
            if (assetForIdentifier == null) {
                System.out.println(type + " with identifier " + identifier + " is not found.");
            } else {
                // TODO: add type restrictions to references' type, e. g. you cannot have palette reference inside of a palette
                this.references.put(uuid, assetForIdentifier);
            }
        }
    }

//    public static void main(String[] args) {
//        TilePaletteData paletteData = new TilePaletteData();
//        paletteData.references = new Map<>();
//        for (int i = 0; i < 10; i++) {
//            GameAsset<Object> garbage = new GameAsset<>("garbage"+i, GameAssetType.TILE_PALETTE);
//            RawAsset rawAsset = new RawAsset(null);
//            rawAsset.metaData = new PaletteMetadata();
//            garbage.dependentRawAssets.add(rawAsset);
//            paletteData.references.add(garbage);
//        }
//        Json json = new Json();
//        json.setOutputType(JsonWriter.OutputType.json);
//        String jsonString = json.prettyPrint(paletteData);
//        System.out.println(jsonString);
//    }
}
