package com.talosvfx.talos.runtime.maps;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.RuntimeContext;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.runtime.assets.GameResourceOwner;

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

		GameAsset<Object> objectGameAsset = GameResourceOwner.readAsset(json, jsonData);
		staticTilesAsset = objectGameAsset;

		float gridPosX = jsonData.getFloat("gridPositionX");
		float gridPosY = jsonData.getFloat("gridPositionY");
		gridPosition = new GridPosition();
		gridPosition.x = gridPosX;
		gridPosition.y = gridPosY;
	}
}
