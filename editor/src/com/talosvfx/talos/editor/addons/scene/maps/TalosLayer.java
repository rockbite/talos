package com.talosvfx.talos.editor.addons.scene.maps;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.widgets.propertyWidgets.ValueProperty;

public class TalosLayer implements Json.Serializable {


	private String name;
	private LayerType type;

//	private GameAsset<PaletteData> //todo hook up to this

	private int mapWidth = 100;
	private int mapHeight = 100;

	@ValueProperty(min = 0.001f, max = 10f)
	private float tileSizeX = 1;

	@ValueProperty(min = 0.001f, max = 10f)
	private float tileSizeY = 1;


	//Layer dependent info, don't use poly to keep it simple

	//One is a 2d array, one is a bag of entities

	IntMap<IntMap<StaticTile>> staticTiles = new IntMap<IntMap<StaticTile>>();
	Array<GameObject> rootEntities = new Array<>();


	protected TalosLayer () {}

	public TalosLayer (String name) {
		this.name = name;
		this.type = LayerType.STATIC;
	}
	public String getName () {
		return name;
	}

	public void setName (String newName) {
		this.name = newName;
	}

	@Override
	public void write (Json json) {
		json.writeValue("type", this.type);
		json.writeValue("name", this.name);
		switch (type) {
		case STATIC:
			serializeForStatic(json);
			break;
		case DYNAMIC_ENTITY:
			serializeForDynamic(json);
			break;
		}
	}

	private void serializeForDynamic (Json json) {
		json.writeArrayStart("entities");
		for (GameObject rootEntity : rootEntities) {
			json.writeValue(rootEntity);
		}
		json.writeArrayEnd();
	}
	private void deserializeForDynamic (Json json, JsonValue jsonData) {
		JsonValue entities = jsonData.get("entities");
		for (JsonValue entity : entities) {
			GameObject object = json.readValue(GameObject.class, entity);
			this.rootEntities.add(object);
		}
	}

	private void serializeForStatic (Json json) {
		json.writeValue("mapWidth", mapWidth);
		json.writeValue("mapHeight", mapHeight);
		json.writeValue("tileSizeX", tileSizeX);
		json.writeValue("tileSizeY", tileSizeY);

		json.writeArrayStart("tiles");
		for (IntMap.Entry<IntMap<StaticTile>> staticTile : staticTiles) {
			int x = staticTile.key;
			IntMap<StaticTile> value = staticTile.value;

			for (IntMap.Entry<StaticTile> staticTileEntry : value) {
				int y = staticTileEntry.key;
				StaticTile tile = staticTileEntry.value;

				json.writeObjectStart();
				json.writeValue(tile);
				json.writeObjectEnd();
			}
		}
		json.writeArrayEnd();
	}

	private void deserializeForStatic (Json json, JsonValue jsonData) {
		this.mapWidth = jsonData.getInt("mapWidth", 100);
		this.mapHeight = jsonData.getInt("mapHeight", 100);
		this.tileSizeX = jsonData.getFloat("tileSizeX", 1);
		this.tileSizeY = jsonData.getFloat("tileSizeY", 1);

		JsonValue tiles = jsonData.get("tiles");
		for (JsonValue tile : tiles) {
			StaticTile readTile = json.readValue(StaticTile.class, tile);
			putTile(readTile);
		}
	}

	private void putTile (StaticTile readTile) {
		GridPosition gridPosition = readTile.gridPosition;

		if (!staticTiles.containsKey(gridPosition.x)) {
			staticTiles.put(gridPosition.x, new IntMap<>());
		}
		IntMap<StaticTile> entries = staticTiles.get(gridPosition.x);
		entries.put(gridPosition.y, readTile);
	}

	@Override
	public void read (Json json, JsonValue jsonData) {
		this.type = json.readValue(LayerType.class, jsonData.get("type"));
		this.name = jsonData.getString("name");

		switch (type) {
		case STATIC:
			deserializeForStatic(json, jsonData);
			break;
		case DYNAMIC_ENTITY:
			deserializeForDynamic(json, jsonData);
			break;
		}
	}



	@Override
	public String toString () {
		return name + " - " + type.toString();
	}

	public LayerType getType () {
		return type;
	}

	public int getMapWidth () {
		return mapWidth;
	}

	public int getMapHeight () {
		return mapHeight;
	}

	public float getTileSizeX () {
		return tileSizeX;
	}

	public float getTileSizeY () {
		return tileSizeY;
	}

	public Array<GameObject> getRootEntities () {
		return rootEntities;
	}

	public IntMap<IntMap<StaticTile>> getStaticTiles () {
		return staticTiles;
	}
}
