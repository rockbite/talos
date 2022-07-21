package com.talosvfx.talos.editor.addons.scene.maps;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.widgets.propertyWidgets.ValueProperty;

public class TalosLayer implements Json.Serializable {



	enum LayerType {
		STATIC,
		DYNAMIC_ENTITY
	}

	private String name;
	private LayerType type;

	private int mapWidth = 100;
	private int mapHeight = 100;

	//Layer dependent info, don't use poly to keep it simple

	//One is a 2d array, one is a bag of entities

	IntMap<IntMap<StaticTile>> staticTiles = new IntMap<IntMap<StaticTile>>();
	Array<GameObject> entities = new Array<>();


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

	}
	private void deserializeForDynamic (Json json, JsonValue jsonData) {

	}

	private void serializeForStatic (Json json) {
		json.writeValue("mapWidth", mapWidth);
		json.writeValue("mapHeight", mapHeight);
	}

	private void deserializeForStatic (Json json, JsonValue jsonData) {

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
}
