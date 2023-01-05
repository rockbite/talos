package com.talosvfx.talos.editor.addons.scene.logic;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.logic.components.AComponent;
import com.talosvfx.talos.editor.notifications.ContextRequiredEvent;

import java.util.Collection;
import java.util.function.Supplier;

public interface GameObjectContainer extends ContextRequiredEvent.Context {

    String getName();
    void setName(String name);
    Array<GameObject> getGameObjects();
    Iterable<AComponent> getComponents();
    void addGameObject(GameObject gameObject);
    Array<GameObject> deleteGameObject(GameObject gameObject);
    void removeObject(GameObject gameObject);
    void addComponent(AComponent component);

    void removeComponent (AComponent component);

    boolean hasGOWithName (String name);

    void clearChildren (Array<GameObject> tmp);

    GameObject getParent();
    GameObject getSelfObject();
    void setParent(GameObject gameObject);

    Supplier<Collection<String>> getAllGONames ();
}
