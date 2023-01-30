package com.talosvfx.talos.runtime.scene;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class Prefab extends SavableContainer {

    public String name;

    public Prefab (FileHandle fileHandle) {
        path = fileHandle.path();
        name = fileHandle.nameWithoutExtension();
        loadFromHandle(fileHandle);
    }

    public Prefab (String jsonContent, String path, String name) {
        this.path = path;
        this.name = name;
        loadFromString(jsonContent);
    }

    public Prefab(GameObject root) {
        this.root = root;
    }

    @Override
    protected void writeData(Json json) {
        json.writeValue("root", root, GameObject.class);
    }

    @Override
    public void load(String data) {
        JsonValue jsonValue = new JsonReader().parse(data);
        Json json = new Json();
        root = json.readValue(GameObject.class, jsonValue.get("root"));
        root.setGameObjectContainer(this);
        name = root.getName();

        //Lets add a fake root
    }

}
