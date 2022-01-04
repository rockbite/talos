package com.talosvfx.talos.editor.addons.scene.logic;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.addons.scene.logic.components.IComponent;

public class Prefab implements GameObjectContainer, Json.Serializable {

    public GameObject root;

    public Prefab() {
        root = new GameObject();
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
    public Iterable<IComponent> getComponents () {
        return root.getComponents();
    }

    @Override
    public void addGameObject (GameObject gameObject) {
        root.addGameObject(gameObject);
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
    public void addComponent (IComponent component) {
        root.addComponent(component);
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
        return root.getParent();
    }

    @Override
    public GameObject getSelfObject () {
        return root.getSelfObject();
    }

    @Override
    public void setParent (GameObject gameObject) {
        root.setParent(gameObject);
    }
}
