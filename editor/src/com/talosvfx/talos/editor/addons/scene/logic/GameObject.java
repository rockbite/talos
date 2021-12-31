package com.talosvfx.talos.editor.addons.scene.logic;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.logic.components.IComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.RendererComponent;
import com.talosvfx.talos.editor.widgets.propertyWidgets.EditableLabelWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;

public class GameObject implements GameObjectContainer, Json.Serializable, IPropertyHolder, IPropertyProvider {

    private String name = "gameObject";

    private Array<GameObject> children;
    private ObjectMap<String, GameObject> childrenMap = new ObjectMap<>();
    private ObjectSet<IComponent> components = new ObjectSet<>();
    private ObjectMap<Class, IComponent> componentClasses = new ObjectMap<>();

    private Array<GameObject> tmp = new Array<>();
    public GameObject parent;

    public static Vector2 tmpVec = new Vector2();

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

        if(children != null) {
            json.writeArrayStart("children");
            for(GameObject child: children) {
                json.writeValue(child, GameObject.class);
            }
            json.writeArrayEnd();
        }
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        name = jsonData.getString("name");

        JsonValue componentsJson = jsonData.get("components");
        for(JsonValue componentJson : componentsJson) {
            IComponent component = json.readValue(IComponent.class, componentJson);
            addComponent(component);
        }

        JsonValue childrenJson = jsonData.get("children");
        if(childrenJson != null) {
            for (JsonValue childJson : childrenJson) {
                GameObject childObject = json.readValue(GameObject.class, childJson);
                addGameObject(childObject);
            }
        }
    }

    @Override
    public void addGameObject (GameObject gameObject) {
        if(children == null) {
            children = new Array<>();
        }

        children.add(gameObject);
        childrenMap.put(gameObject.name, gameObject);

        gameObject.setParent(this);
    }

    @Override
    public  Array<GameObject> deleteGameObject (GameObject gameObject) {
        tmp.clear();
        if(children == null) {
            return tmp;
        }

        String name = gameObject.getName();
        if(childrenMap.containsKey(name)) {
            GameObject objectToRemove = childrenMap.get(name);
            childrenMap.remove(name);
            children.removeValue(objectToRemove, true);
            tmp.add(objectToRemove);

            objectToRemove.clearChildren(tmp);
        }

        if(children.isEmpty()) children = null;

        return tmp;
    }

    @Override
    public void removeObject (GameObject gameObject) {
        if(children == null) {
            return;
        }

        String name = gameObject.getName();
        if(childrenMap.containsKey(name)) {
            GameObject objectToRemove = childrenMap.get(name);
            childrenMap.remove(name);
            children.removeValue(objectToRemove, true);
        }

        if(children.isEmpty()) children = null;
    }

    @Override
    public void clearChildren (Array<GameObject> tmp) {
        if(children == null) return;
        tmp.addAll(children);

        children = null;
        childrenMap.clear();
    }

    @Override
    public GameObject getParent () {
        return parent;
    }

    @Override
    public GameObject getSelfObject () {
        return this;
    }

    @Override
    public void setParent (GameObject gameObject) {
        parent = gameObject;
    }

    @Override
    public void addComponent (IComponent component) {
        components.add(component);
        componentClasses.put(component.getClass(), component);
    }

    @Override
    public boolean hasGOWithName (String name) {
        if(children == null) return false;

        if(childrenMap.containsKey(name)) return true;
        for(GameObject child: children) {
            if(child.hasGOWithName(name)) {
                return true;
            }
        }

        return false;
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

        EditableLabelWidget labelWidget = new EditableLabelWidget("Name") {
            @Override
            public String getValue () {
                return name;
            }

            @Override
            public void valueChanged (String value) {
                SceneEditorAddon.get().workspace.changeGOName(GameObject.this, value);
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

    public boolean hasComponent(Class clazz) {
        if(componentClasses.containsKey(clazz)) {
            return true;
        }

        return false;
    }

    public boolean hasComponentType(Class clazz) {
        for(Class clazzToCheck: componentClasses.keys()) {
            if(clazz.isAssignableFrom(clazzToCheck)) {
                return true;
            }
        }

        return false;
    }

    public <T extends IComponent> T getComponent (Class<? extends T> clazz) {
        return (T) componentClasses.get(clazz);
    }

    @Override
    public void setName (String name) {
        String oldName = this.name;
        this.name = name;
        if(parent != null) {
           parent.notifyChildRename(oldName, name);
        }
    }

    private void notifyChildRename (String oldName, String name) {
        GameObject gameObject = childrenMap.get(oldName);
        childrenMap.remove(oldName);
        childrenMap.put(name, gameObject);
    }

    public Array<GameObject> getChildrenByComponent (Class<? extends IComponent> clazz, Array<GameObject> list) {
        if(children == null) return list;
        for(GameObject gameObject: children) {
            if(gameObject.hasComponentType(clazz)) {
                list.add(gameObject);
            }
            if(gameObject.getGameObjects() != null && gameObject.getGameObjects().size > 0) {
                gameObject.getChildrenByComponent(clazz, list);
            }
        }

        return list;
    }

    public <T extends IComponent> T getComponentSlow (Class<? extends T> clazz) {
        for(Class clazzToCheck: componentClasses.keys()) {
            if(clazz.isAssignableFrom(clazzToCheck)) {
                return (T) componentClasses.get(clazzToCheck);
            }
        }

        return null;
    }
}
