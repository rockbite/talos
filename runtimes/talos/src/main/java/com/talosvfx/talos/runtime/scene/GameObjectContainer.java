package com.talosvfx.talos.runtime.scene;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.runtime.assets.TalosContextProvider;
import com.talosvfx.talos.runtime.scene.components.AComponent;
import com.talosvfx.talos.runtime.utils.Supplier;

import java.util.Collection;
public interface GameObjectContainer extends TalosContextProvider {

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
