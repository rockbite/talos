package com.talosvfx.talos.editor.addons.scene.maps;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.widgets.propertyWidgets.ValueProperty;

public class TalosLayer implements Json.Serializable {


	private String name;
	private LayerType type;

	private int mapWidth = 100;
	private int mapHeight = 100;

	private int tileSizeX = 1;
	private int tileSizeY = 1;


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
	}

	private void deserializeForStatic (Json json, JsonValue jsonData) {
		this.mapWidth = jsonData.getInt("mapWidth", 100);
		this.mapHeight = jsonData.getInt("mapHeight", 100);
		this.tileSizeX = jsonData.getInt("tileSizeX", 1);
		this.tileSizeY = jsonData.getInt("tileSizeY", 1);
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
}
