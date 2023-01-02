package com.talosvfx.talos.editor.addons.scene.logic;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.*;
import com.talosvfx.talos.editor.addons.scene.events.GameObjectActiveChanged;
import com.talosvfx.talos.editor.addons.scene.events.GameObjectNameChanged;
import com.talosvfx.talos.editor.addons.scene.events.commands.GONameChangeCommand;
import com.talosvfx.talos.editor.addons.scene.logic.components.AComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.GameResourceOwner;
import com.talosvfx.talos.editor.addons.scene.logic.components.RendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import com.talosvfx.talos.editor.addons.scene.widgets.gizmos.Gizmo;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.widgets.propertyWidgets.EditableLabelWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.talosvfx.talos.editor.widgets.propertyWidgets.LabelWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.WidgetFactory;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Supplier;

public class GameObject implements GameObjectContainer, Json.Serializable, IPropertyHolder, IPropertyProvider {

    private String name = "gameObject";
    public UUID uuid;

    private String prefabLink = null;

    public boolean active = true;
    private boolean editorTransformLocked = false;
    private boolean editorVisible = true;

    private Array<GameObject> children;
    private ObjectMap<String, GameObject> childrenMap = new ObjectMap<>();
    private ObjectSet<AComponent> components = new ObjectSet<>();
    private ObjectMap<Class, AComponent> componentClasses = new ObjectMap<>();

    private Array<GameObject> tmp = new Array<>();
    public GameObject parent;

    public static Vector2 tmpVec = new Vector2();

    private transient Gizmo.TransformSettings transformSettings = new Gizmo.TransformSettings();
    public transient boolean isPlacing = false;

    @Getter
    private GameObjectContainer rootGameObjectContainer;

    public GameObject () {
        uuid = UUID.randomUUID();
    }

    @Override
    public Array<GameObject> getGameObjects () {
        return children;
    }

    @Override
    public Iterable<AComponent> getComponents () {
        return components;
    }

    @Override
    public String getName () {
        return name;
    }

    @Override
    public Class<? extends IPropertyProvider> getType() {
        return getClass();
    }

    @Override
    public void write (Json json) {
        json.writeValue("name", name);
        json.writeValue("uuid", uuid.toString());
        json.writeValue("prefabLink", prefabLink);
        json.writeValue("active", active);
        json.writeValue("visible", editorVisible);
        json.writeValue("locked", editorTransformLocked);

        json.writeArrayStart("components");
        for(AComponent component: components) {
            json.writeValue(component, AComponent.class);
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
        if (jsonData.has("uuid")) {
            uuid = UUID.fromString(jsonData.getString("uuid"));
        } else {
            uuid = UUID.randomUUID();
        }
        prefabLink = jsonData.getString("prefabLink", null);
        active = jsonData.getBoolean("active", this.active);
        editorTransformLocked = jsonData.getBoolean("locked", this.editorTransformLocked);
        editorVisible = jsonData.getBoolean("visible", this.editorVisible);

        JsonValue componentsJson = jsonData.get("components");
        for(JsonValue componentJson : componentsJson) {
            AComponent component = json.readValue(AComponent.class, componentJson);
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

    private ArrayList<String> goNames = new ArrayList<>();
    @Override
    public Supplier<Collection<String>> getAllGONames () {
        goNames.clear();
        addNamesToList(goNames, this);
        return new Supplier<Collection<String>>() {
            @Override
            public Collection<String> get () {
                return goNames;
            }
        };
    }

    private void addNamesToList (ArrayList<String> goNames, GameObject gameObject) {
        goNames.add(gameObject.getName());
        if (gameObject.getGameObjects() != null) {
            Array<GameObject> gameObjects = gameObject.getGameObjects();
            for (int i = 0; i < gameObjects.size; i++) {
                GameObject child = gameObjects.get(i);
                addNamesToList(goNames, child);

            }
        }
    }

    @Override
    public void addComponent (AComponent component) {
        components.add(component);
        component.setGameObject(this);
        componentClasses.put(component.getClass(), component);
    }

    @Override
    public void removeComponent (AComponent component) {
        components.remove(component);
        componentClasses.remove(component.getClass());
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

        LabelWidget uuidWidget = new LabelWidget("UUID", new Supplier<String>() {
            @Override
            public String get () {
                return uuid.toString();
            }
        });

        EditableLabelWidget labelWidget = new EditableLabelWidget("Name", new Supplier<String>() {
            @Override
            public String get() {
                return name;
            }
        }, new PropertyWidget.ValueChanged<String>() {
            @Override
            public void report(String value) {
                GONameChangeCommand command = Notifications.obtainEvent(GONameChangeCommand.class).set(GameObject.this, value);
                Notifications.fireEvent(command);
            }
        });

        properties.add(labelWidget);
        properties.add(uuidWidget);

        PropertyWidget activeWidget = WidgetFactory.generate(this, "active", "Active");
        activeWidget.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent event, Actor actor) {
                GameObjectActiveChanged activeChanged = Notifications.obtainEvent(GameObjectActiveChanged.class);
                activeChanged.target = GameObject.this;
                Notifications.fireEvent(activeChanged);
            }
        });
        properties.add(activeWidget);

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

    public GameResourceOwner<?> getRenderResourceComponent () {
        for (AComponent component : components) {
            if (component instanceof GameResourceOwner && component instanceof RendererComponent) {
                return (GameResourceOwner<?>)component;
            }
        }
        return null;
    }

    public GameResourceOwner<?> getResourceComponent () {
        for (AComponent component : components) {
            if (component instanceof GameResourceOwner) {
                return (GameResourceOwner<?>)component;
            }
        }
        return null;
    }

    public <T> T findComponent (Class<T> clazz) {
        for (AComponent component : components) {
            if (clazz.isAssignableFrom(component.getClass())) {
                return (T) component;
            }
        }
        return null;

    }

    public <T extends AComponent> T getComponent (Class<? extends T> clazz) {
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

    public Array<GameObject> getChildrenByComponent (Class<?> clazz, Array<GameObject> list) {
        if (hasComponentType(clazz)) { //Check self in case of prefab
            list.add(this);
        }
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

    public <T extends AComponent> T getComponentAssignableFrom (Class<? extends T> clazz) {
        for(Class clazzToCheck: componentClasses.keys()) {
            if(clazz.isAssignableFrom(clazzToCheck)) {
                return (T) componentClasses.get(clazzToCheck);
            }
        }

        return null;
    }

    public void setPrefabLink (String prefabLink) {
        this.prefabLink = prefabLink;
    }

    BoundingBox boundingBox = new BoundingBox();

    private static void estimateSizeForGameObject (GameObject gameObject, BoundingBox minMax) {
        Iterable<AComponent> components = gameObject.getComponents();
        for (AComponent component : components) {
            if (component instanceof RendererComponent) {
                ((RendererComponent)component).minMaxBounds(gameObject, minMax);
            }
        }
        if (gameObject.children != null) {
            for (GameObject child : gameObject.children) {
                estimateSizeForGameObject(child, minMax);
            }
        }
    }
    public BoundingBox estimateSizeFromRoot () {
        boundingBox.clr();
        estimateSizeForGameObject(this, boundingBox);
        return boundingBox;
    }


    public Gizmo.TransformSettings getTransformSettings () {
        return transformSettings;
    }

    public static void setPositionFromWorldPosition (GameObject object, Vector2 worldPosition) {
        TransformComponent transformComponent = object.getComponent(TransformComponent.class);
        transformComponent.worldPosition.set(worldPosition);
        projectInParentSpace(object.parent, object);
    }

    public static void projectInParentSpace(GameObject parentToMoveTo, GameObject childThatHasMoved) {
        if (childThatHasMoved.hasComponent(TransformComponent.class)) {
            TransformComponent childPositionComponent = childThatHasMoved.getComponent(TransformComponent.class);
            TransformComponent parentPositionComponent = new TransformComponent();
            if (parentToMoveTo.hasComponent(TransformComponent.class)) {
                parentPositionComponent = parentToMoveTo.getComponent(TransformComponent.class);
            }

            Vector2 tmp = TransformComponent.vec;
            tmp.set(childPositionComponent.worldPosition);
            tmp.sub(parentPositionComponent.worldPosition);
            childPositionComponent.position.set(tmp);
            childPositionComponent.rotation -= parentPositionComponent.rotation;

            tmp.set(1 / parentPositionComponent.worldScale.x, 1 / parentPositionComponent.worldScale.y);
            childPositionComponent.position.scl(tmp);

            tmp.set(childPositionComponent.worldScale);
            tmp.scl(1 / parentPositionComponent.worldScale.x, 1 / parentPositionComponent.worldScale.y);
            childPositionComponent.scale.set(tmp);
        }
    }

    public int getParentCount () {
        int count = 0;
        GameObject par = getParent();

        while (par != null) {
            par = par.getParent();
            count++;
        }

        return count;
    }

    public GameObject getTopParent (GameObject parentToIgnore) {
        GameObject topParent = this;
        while (topParent.getParent() != null && topParent.getParent() != parentToIgnore) {
            topParent = topParent.getParent();
        }
        return topParent;
    }

    public GameObject getChildByUUID (String uuid) {
        if (children == null) {
            return null;
        }

        for (int i = 0; i < children.size; i++) {
            GameObject child = children.get(i);
            if (child.uuid.toString().equals(uuid)) {
                return child;
            }

            if (child.getGameObjects() != null) {
                GameObject childByUUID = child.getChildByUUID(uuid);
                if (childByUUID != null) {
                    return childByUUID;
                }
            }
        }

        return null;
    }

    public void setEditorVisible (boolean editorVisible){
        this.editorVisible = editorVisible;
        if(children !=null) {
            for (GameObject child : children) {
                child.setEditorVisible(editorVisible);
            }
        }
    }

    public boolean isEditorVisible (){
        return editorVisible;
    }

    public void setEditorTransformLocked (boolean editorTransformLocked){
        this.editorTransformLocked = editorTransformLocked;
        if(children !=null) {
            for (GameObject child : children) {
                child.setEditorTransformLocked(editorTransformLocked);
            }
        }
    }

    public boolean isEditorTransformLocked (){
        return editorTransformLocked;
    }

    public GameObjectContainer getGameObjectContainerRoot () {
        if (getParent() == null) {
            return rootGameObjectContainer;
        } else {
            return getParent().getGameObjectContainerRoot();
        }
    }

    public void setGameObjectContainer (GameObjectContainer rootGameObjectContainer) {
        this.rootGameObjectContainer = rootGameObjectContainer;
    }

    public Array<GameObject> findGOsWithComponents(Array<GameObject> result, Class<? extends AComponent> clazz) {
        if(hasComponent(clazz)) {
            result.add(this);
            return result;
        }

        if(children != null) {
            for (GameObject child : children) {
                child.findGOsWithComponents(result, clazz);
            }
        }

        return result;
    }
}
