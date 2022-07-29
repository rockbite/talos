package com.talosvfx.talos.editor.addons.scene.maps;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.GameAssetType;

public class StaticTile implements Json.Serializable {

	GameAsset<?> staticTilesAsset;
	GridPosition gridPosition;

	public StaticTile() { }

	public StaticTile(GameAsset<?> gameAsset, GridPosition gridPosition) {
		staticTilesAsset = gameAsset;
		this.gridPosition = gridPosition;
	}

	public GameAsset<?> getStaticTilesAsset () {
		return staticTilesAsset;
	}

	public GridPosition getGridPosition () {
		return gridPosition;
	}

	@Override
	public void write (Json json) {
		json.writeValue("nameIdentifier", staticTilesAsset.nameIdentifier);
		json.writeValue("type", staticTilesAsset.type);
		json.writeValue("gridPositionX", gridPosition.x);
		json.writeValue("gridPositionY", gridPosition.y);
	}

	@Override
	public void read (Json json, JsonValue jsonData) {
		String nameIdentifier = jsonData.getString("nameIdentifier");
		GameAssetType type = json.readValue(GameAssetType.class, jsonData.get("type"));

		staticTilesAsset = AssetRepository.getInstance().getAssetForIdentifier(nameIdentifier, type);

		float gridPosX = jsonData.getFloat("gridPositionX");
		float gridPosY = jsonData.getFloat("gridPositionY");
		gridPosition = new GridPosition();
		gridPosition.x = gridPosX;
		gridPosition.y = gridPosY;
	}
}
