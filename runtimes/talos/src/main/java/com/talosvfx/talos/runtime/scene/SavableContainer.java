package com.talosvfx.talos.runtime.scene;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.talosvfx.talos.runtime.scene.components.AComponent;
import com.talosvfx.talos.runtime.utils.TempHackUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

public abstract class SavableContainer implements GameObjectContainer, Json.Serializable {

	private static final Logger logger = LoggerFactory.getLogger(SavableContainer.class);

	public GameObject root;

	public SavableContainer () {
		root = new GameObject();
		root.setGameObjectContainer(this);
	}

	@Override
	public void write (Json json) {
		root.write(json);
	}

	@Override
	public void read (Json json, JsonValue jsonData) {
		root.read(json, jsonData);
	}

	@Override
	public String getName () {
		return root.getName();
	}

	@Override
	public void setName (String name) {
		root.setName(name);
	}

	@Override
	public Array<GameObject> getGameObjects () {
		return root.getGameObjects();
	}

	@Override
	public Iterable<AComponent> getComponents () {
		return null;
	}

	@Override
	public void addGameObject (GameObject gameObject) {
		root.addGameObject(gameObject);
		gameObject.setParent(root);
	}

	@Override
	public Array<GameObject> deleteGameObject (GameObject gameObject) {
		return root.deleteGameObject(gameObject);
	}

	@Override
	public void removeObject (GameObject gameObject) {
		root.removeObject(gameObject);
	}

	@Override
	public void addComponent (AComponent component) {

	}

	@Override
	public void removeComponent (AComponent component) {

	}

	@Override
	public boolean hasGOWithName (String name) {
		return root.hasGOWithName(name);
	}

	@Override
	public void clearChildren (Array<GameObject> tmp) {
		root.clearChildren(tmp);
	}

	@Override
	public GameObject getParent () {
		return null;
	}

	@Override
	public GameObject getSelfObject () {
		return root;
	}

	@Override
	public void setParent (GameObject gameObject) {
		// do nothing
	}

	private ArrayList<String> goNames = new ArrayList<>();

	@Override
	public Supplier<Collection<String>> getAllGONames () {
		goNames.clear();
		addNamesToList(goNames, root);
		return new Supplier<Collection<String>>() {
			@Override
			public Collection<String> get () {
				return goNames;
			}
		};
	}

	private void addNamesToList (ArrayList<String> goNames, GameObject gameObject) {
		goNames.add(gameObject.getName());
		if (gameObject.getGameObjects() != null) {
			Array<GameObject> gameObjects = gameObject.getGameObjects();
			for (int i = 0; i < gameObjects.size; i++) {
				GameObject child = gameObjects.get(i);
				addNamesToList(goNames, child);

			}
		}
	}

	protected void writeData (Json json) {
		json.writeArrayStart("gameObjects");
		Array<GameObject> gameObjects = getGameObjects();
		if (gameObjects != null) {
			for (GameObject gameObject : gameObjects) {
				json.writeValue(gameObject);
			}
		}
		json.writeArrayEnd();
	}

	public String getAsString () {
		try {

			StringWriter stringWriter = new StringWriter();
			Json json = new Json();
			json.setOutputType(JsonWriter.OutputType.json);
			json.setWriter(stringWriter);
			json.getWriter().object();

			writeData(json);

			String finalString = stringWriter.toString() + "}";

			return finalString;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error saving scene", e);
			return null;
		}
	}

	public void load (String data) {
		JsonValue jsonValue = new JsonReader().parse(data);
		Json json = new Json();
		JsonValue gameObjectsJson = jsonValue.get("gameObjects");
		root = new GameObject();
		root.setGameObjectContainer(this);
		for (JsonValue gameObjectJson : gameObjectsJson) {
			GameObject gameObject = json.readValue(GameObject.class, gameObjectJson);
			root.addGameObject(gameObject);
		}
	}

	public void loadFromHandle (FileHandle handle) {
		loadFromString(handle.readString());
	}

	void loadFromString (String jsonString) {
		load(TempHackUtil.hackIt(jsonString));
	}


	public Array<GameObject> findGameObjects(String targetString) {
		Array<GameObject> list = new Array<>();

		if(targetString.isEmpty()) {
			list.add(root);
			return list;
		}

		findGameObjects(list, root, targetString);

		return list;
	}

	public void findGameObjects(Array<GameObject> list, GameObject parent, String targetString) {

		int dotIndex = targetString.indexOf(".");
		String lastPart = "";

		String levelName = targetString;
		if(dotIndex >= 0) {
			levelName = targetString.substring(0, targetString.indexOf("."));

			if(targetString.length() > dotIndex + 1) {
				lastPart = targetString.substring(targetString.indexOf(".") + 1);
			} else {
				//irrelevant dot
				lastPart = "";
			}
		}

		Array<GameObject> gameObjects = parent.getGameObjects();

		if(gameObjects == null) return;

		for(GameObject gameObject : gameObjects) {

			boolean matchCriteria = false;
			if(levelName.contains("*")) {
				String expression = levelName.replaceAll("\\*", ".*");
				matchCriteria = gameObject.getName().matches(expression);
			} else {
				matchCriteria = gameObject.getName().equals(levelName);
			}

			if(matchCriteria) {
				if(lastPart.length() == 0) {
					list.add(gameObject);
				} else {
					findGameObjects(list, gameObject, lastPart);
				}
			}
		}
	}
}
