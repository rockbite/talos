package com.talosvfx.talos.editor.addons.scene.logic;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.logic.components.IComponent;

public interface GameObjectContainer {

    String getName();
    Array<GameObject> getGameObjects();
    Iterable<IComponent> getComponents();
    void addGameObject(GameObject gameObject);
    Array<GameObject> deleteGameObject(GameObject gameObject);
    void addComponent(IComponent component);

    boolean hasGOWithName (String name);

    void clearChildren (Array<GameObject> tmp);

    GameObject getParent();
    void setParent(GameObject gameObject);
}
