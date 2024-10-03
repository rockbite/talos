package com.talosvfx.talos.editor.addons.scene.logic.componentwrappers;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.logic.PropertyWrapperProviders;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.components.AComponent;
import com.talosvfx.talos.runtime.scene.components.BoneComponent;
import lombok.Getter;

public class GameObjectPropertyHolder extends PropertyWrapperProviders.ObjectPropertyHolder<GameObject> {
    @Getter
    private final GameObject gameObject;
    private final GameObjectPropertyProvider gameObjectPropertyProvider;

    public GameObjectPropertyHolder (GameObject gameObject) {
        this.gameObject = gameObject;
        gameObjectPropertyProvider = new GameObjectPropertyProvider(gameObject);
    }

    @Override
    public Iterable<IPropertyProvider> getPropertyProviders () {
        Array<IPropertyProvider> list = new Array<>();

        list.add(gameObjectPropertyProvider);

        for (AComponent component : gameObject.getComponents()) {
            list.add(PropertyWrapperProviders.getOrCreateProvider(component));
        }

        return list;
    }

    @Override
    public String getName () {
        return gameObject.getName();
    }
}
