package com.talosvfx.talos.editor.addons.scene.logic;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.addons.scene.logic.components.IComponent;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;

public class Prefab extends SavableContainer {

    public String name;

    public static Prefab from(FileHandle fileHandle) {
        Prefab prefab = new Prefab();
        prefab.path = fileHandle.path();
        prefab.loadFromPath();

        prefab.name = fileHandle.nameWithoutExtension();

        return prefab;
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
    }

    @Override
    public Iterable<IPropertyProvider> getPropertyProviders() {
        return null;
    }
}
