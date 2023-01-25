package com.talosvfx.talos.runtime.maps;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.RuntimeContext;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.runtime.assets.GameResourceOwner;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.ValueProperty;
import com.talosvfx.talos.runtime.scene.components.TransformComponent;

import java.util.UUID;

public class TalosLayer implements GameResourceOwner<TilePaletteData>, Json.Serializable {


	private String name;
	private LayerType type;

	private GameAsset<TilePaletteData> gameAsset;

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

	public transient GameObject entityPlacing;


	protected TalosLayer () {}

	public TalosLayer (String name) {
		this.name = name;
		this.type = LayerType.DYNAMIC_ENTITY; // lets have dynamic the default
	}
	public String getName () {
		return name;
	}

	public void setName (String newName) {
		this.name = newName;
	}

	@Override
	public void write (Json json) {
		if (gameAsset != null) { //can be null
			GameResourceOwner.writeGameAsset(json, this);
		}

		json.writeValue("type", this.type);
		json.writeValue("name", this.name);

		json.writeValue("mapWidth", mapWidth);
		json.writeValue("mapHeight", mapHeight);

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
		json.writeValue("tileSizeX", tileSizeX);
		json.writeValue("tileSizeY", tileSizeY);

		json.writeArrayStart("tiles");
		for (IntMap.Entry<IntMap<StaticTile>> staticTile : staticTiles) {
			int x = staticTile.key;
			IntMap<StaticTile> value = staticTile.value;

			for (IntMap.Entry<StaticTile> staticTileEntry : value) {
				int y = staticTileEntry.key;
				StaticTile tile = staticTileEntry.value;
				json.writeValue(tile);
			}
		}
		json.writeArrayEnd();
	}

	private void deserializeForStatic (Json json, JsonValue jsonData) {
		this.tileSizeX = jsonData.getFloat("tileSizeX", 1);
		this.tileSizeY = jsonData.getFloat("tileSizeY", 1);

		JsonValue tiles = jsonData.get("tiles");
		for (JsonValue tile : tiles) {
			StaticTile readTile = json.readValue(StaticTile.class, tile);
			putTile(readTile);
		}
	}

	public void removeTile (int x, int y) {
		if (staticTiles.containsKey(x)) {
			IntMap<StaticTile> entries = staticTiles.get(x);
			if (entries.containsKey(y)) {
				entries.remove(y);
			}
		}
	}

	private void putTile (StaticTile readTile) {
		GridPosition gridPosition = readTile.gridPosition;

		if (!staticTiles.containsKey(gridPosition.getIntX())) {
			staticTiles.put(gridPosition.getIntX(), new IntMap<>());
		}
		IntMap<StaticTile> entries = staticTiles.get(gridPosition.getIntX());
		entries.put(gridPosition.getIntY(), readTile);
	}

	@Override
	public void read (Json json, JsonValue jsonData) {
		UUID gameResourceUUID = GameResourceOwner.readGameResourceUUIDFromComponent(jsonData);
		if (gameResourceUUID == null) {
			String gameResourceIdentifier = GameResourceOwner.readGameResourceFromComponent(jsonData);
			loadPaletteFromIdentifier(gameResourceIdentifier);
		} else {
			loadPaletteFromUniqueIdentifier(gameResourceUUID);
		}

		this.type = json.readValue(LayerType.class, jsonData.get("type"));
		this.name = jsonData.getString("name");

		this.mapWidth = jsonData.getInt("mapWidth", 100);
		this.mapHeight = jsonData.getInt("mapHeight", 100);

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

	@Override
	public GameAssetType getGameAssetType () {
		return GameAssetType.TILE_PALETTE;
	}

	@Override
	public GameAsset<TilePaletteData> getGameResource () {
		return gameAsset;
	}

	GameAsset.GameAssetUpdateListener gameAssetUpdateListener = new GameAsset.GameAssetUpdateListener() {
		@Override
		public void onUpdate () {
			if (gameAsset.isBroken()) {
			} else {
			}
		}
	};

	@Override
	public void setGameAsset (GameAsset<TilePaletteData> newGameAsset) {
		if (this.gameAsset != null) {
			//Remove from old game asset, it might be the same, but it may also have changed
			this.gameAsset.listeners.removeValue(gameAssetUpdateListener, true);
		}

		this.gameAsset = newGameAsset;

		if (newGameAsset != null) {
			this.gameAsset.listeners.add(gameAssetUpdateListener);

			gameAssetUpdateListener.onUpdate();
		}
	}

	private void loadPaletteFromIdentifier (String identifier) {
		GameAsset<TilePaletteData> assetForIdentifier = RuntimeContext.getInstance().AssetRepository.getAssetForIdentifier(identifier, GameAssetType.TILE_PALETTE);
		setGameAsset(assetForIdentifier);
	}

	private void loadPaletteFromUniqueIdentifier (UUID uniqueIdentifier) {
		GameAsset<TilePaletteData> assetForUniqueIdentifier = RuntimeContext.getInstance().AssetRepository.getAssetForUniqueIdentifier(uniqueIdentifier, GameAssetType.TILE_PALETTE);
		setGameAsset(assetForUniqueIdentifier);
	}

	public void setStaticTile (StaticTile staticTile) {
		putTile(staticTile);
	}

	static Vector2 temp = new Vector2();
	public void removeEntity (float x, float y) {
		float smallestDistance = Float.MAX_VALUE;
		GameObject target = null;
		for (GameObject rootEntity : getRootEntities()) {
			if (rootEntity.hasComponent(TransformComponent.class)) {
				TransformComponent component = rootEntity.getComponent(TransformComponent.class);
				float dst = temp.set(x, y).dst(component.position);
				if (dst < smallestDistance) {
					smallestDistance = dst;
					target = rootEntity;
				}
			}
		}

		if (target != null) {
			getRootEntities().removeValue(target, true);
		}
	}

	public void removeEntity (GameObject entityUnderMouse) {
		getRootEntities().removeValue(entityUnderMouse, true);
	}
}
