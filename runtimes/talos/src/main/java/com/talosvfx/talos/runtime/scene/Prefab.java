package com.talosvfx.talos.runtime.scene;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class Prefab extends SavableContainer {
    public static String PREFIX = "Prefab_";

    public transient String name;

    public Prefab (FileHandle fileHandle) {
        loadFromHandle(fileHandle);
    }

    public Prefab (String jsonContent, String name) {
        loadFromString(jsonContent);

        setName(name);
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

        //Lets add a fake root
    }

    @Override
    public void loadFromHandle(FileHandle handle) {
        super.loadFromHandle(handle);
        setName(handle.nameWithoutExtension());
    }

    public void setName(String name) {
        if (name.startsWith(PREFIX)) {
            this.name = name;
        } else {
            this.name = PREFIX + name;
        }
        this.root.setName(this.name);
    }
}
