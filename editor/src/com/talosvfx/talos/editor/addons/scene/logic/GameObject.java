package com.talosvfx.talos.editor.addons.scene.logic;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectSet;
import com.talosvfx.talos.editor.addons.scene.logic.components.IComponent;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.talosvfx.talos.editor.widgets.propertyWidgets.LabelWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;

public class GameObject implements GameObjectContainer, Json.Serializable, IPropertyHolder, IPropertyProvider {

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

        json.writeArrayStart("components");
        for(IComponent component: components) {
            json.writeValue(component, IComponent.class);
        }
        json.writeArrayEnd();
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        name = jsonData.getString("name");

        JsonValue componentsJson = jsonData.get("components");
        for(JsonValue componentJson : componentsJson) {
            IComponent component = json.readValue(IComponent.class, componentJson);
            components.add(component);
        }
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

    @Override
    public Iterable<IPropertyProvider> getPropertyProviders () {
        Array<IPropertyProvider> list = new Array<>();

        list.add(this);

        for(IPropertyProvider provider: components) {
            list.add(provider);
        }

        return list;
    }

    @Override
    public Array<PropertyWidget> getListOfProperties () {
        Array<PropertyWidget> properties = new Array<>();

        LabelWidget labelWidget = new LabelWidget("Name") {
            @Override
            public String getValue () {
                return name;
            }
        };

        properties.add(labelWidget);

        return properties;
    }

    @Override
    public String getPropertyBoxTitle () {
        return "Game Object";
    }

    @Override
    public int getPriority () {
        return 0;
    }
}
