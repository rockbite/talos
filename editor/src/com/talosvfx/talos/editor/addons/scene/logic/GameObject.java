package com.talosvfx.talos.editor.addons.scene.logic;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectSet;
import com.talosvfx.talos.editor.addons.scene.logic.components.IComponent;

public class GameObject implements GameObjectContainer, Json.Serializable {

    private String name = "gameObject";

    private Array<GameObject> children;
    private ObjectSet<IComponent> components = new ObjectSet<>();

    @Override
    public Array<GameObject> getGameObjects () {
        return children;
    }

    @Override
    public Iterable<IComponent> getComponents () {
        return components;
    }

    @Override
    public String getName () {
        return name;
    }

    @Override
    public void write (Json json) {
        json.writeValue("name", name);
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        name = jsonData.getString("name");
    }

    @Override
    public void addGameObject (GameObject gameObject) {
        if(children == null) {
            children = new Array<>();
        }

        children.add(gameObject);
    }

    @Override
    public void addComponent (IComponent component) {
        components.add(component);
    }
}
