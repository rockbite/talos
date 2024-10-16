package com.talosvfx.talos.runtime.scene;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.*;
import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.Skeleton;
import com.talosvfx.talos.runtime.assets.GameResourceOwner;
import com.talosvfx.talos.runtime.assets.TalosContextProvider;
import com.talosvfx.talos.runtime.routine.RoutineEventInterface;
import com.talosvfx.talos.runtime.routine.RoutineEventListener;
import com.talosvfx.talos.runtime.scene.components.*;
import com.talosvfx.talos.runtime.scene.utils.TransformSettings;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyWrapper;
import com.talosvfx.talos.runtime.utils.Supplier;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class GameObject implements GameObjectContainer, RoutineEventListener, Json.Serializable {

    private transient String talosContextIdentifier;

    private String name = "gameObject";
    public UUID uuid;

    private String prefabLink = null;

    public boolean active = true;
    private boolean editorTransformLocked = false;
    private boolean editorVisible = true;

    private Array<GameObject> children = new Array<>();
    private ObjectMap<String, GameObject> childrenMap = new ObjectMap<>();
    private ObjectSet<AComponent> components = new ObjectSet<>();
    private ObjectMap<Class, AComponent> componentClasses = new ObjectMap<>();

    private Array<GameObject> tmp = new Array<>();
    public GameObject parent;

    private GameObjectContainer rootGameObjectContainer;

    public boolean isPlacing;

    public boolean hierarchyDirty = true;

    //ready only
    private transient String readBoneName;

    @Getter
    private transient TransformSettings transformSettings = new TransformSettings();

    private Array<RoutineEventInterface> routineEventListeners;

    int componentBitMask = 0;
    boolean maskDirty = true;

//BoneComponent (com.talosvfx.talos.runtime.scene.components)
//CameraComponent (com.talosvfx.talos.runtime.scene.components)
//CurveComponent (com.talosvfx.talos.runtime.scene.components)
//DataComponent (com.talosvfx.talos.runtime.scene.components)
//EdgeCollider2DComponent (com.talosvfx.talos.runtime.scene.components)
//PaintSurfaceComponent (com.talosvfx.talos.runtime.scene.components)
//RendererComponent (com.talosvfx.talos.runtime.scene.components)
//MapComponent (com.talosvfx.talos.runtime.scene.components)
//ParticleComponent (com.talosvfx.talos.runtime.scene.components)
//PathRendererComponent (com.talosvfx.talos.runtime.scene.components)
//RoutineRendererComponent (com.talosvfx.talos.runtime.scene.components)
//SpineRendererComponent (com.talosvfx.talos.runtime.scene.components)
//SpriteRendererComponent (com.talosvfx.talos.runtime.scene.components)
//ScriptComponent (com.talosvfx.talos.runtime.scene.components)
//TileDataComponent (com.talosvfx.talos.runtime.scene.components)
//TransformComponent (com.talosvfx.talos.runtime.scene.components)


    private static final int BONE_COMPONENT_BIT = 1 << 0;
    private static final int CAMERA_COMPONENT_BIT = 1 << 1;
    private static final int CURVE_COMPONENT_BIT = 1 << 2;
    private static final int DATA_COMPONENT_BIT = 1 << 3;
    private static final int EDGE_COLLIDER_2D_COMPONENT_BIT = 1 << 4;
    private static final int PAINT_SURFACE_COMPONENT_BIT = 1 << 5;
    private static final int RENDERER_COMPONENT_BIT = 1 << 6;
    private static final int MAP_COMPONENT_BIT = 1 << 7;
    private static final int PARTICLE_COMPONENT_BIT = 1 << 8;
    private static final int PATH_RENDERER_COMPONENT_BIT = 1 << 9;
    private static final int ROUTINE_RENDERER_COMPONENT_BIT = 1 << 10;
    private static final int SPINE_RENDERER_COMPONENT_BIT = 1 << 11;
    private static final int SPRITE_RENDERER_COMPONENT_BIT = 1 << 12;
    private static final int SCRIPT_COMPONENT_BIT = 1 << 13;
    private static final int TILE_DATA_COMPONENT_BIT = 1 << 14;
    private static final int TRANSFORM_COMPONENT_BIT = 1 << 15;




    private transient TransformComponent transformComponentCache;
    private transient SpineRendererComponent spineComponentCache;
    private transient BoneComponent boneComponentCache;
    private transient SpriteRendererComponent spriteComponentCache;
    private transient ParticleComponent particleComponentCache;
    private transient RoutineRendererComponent routineRendererComponentCache;

    private void recalculateBitMask () {
        if (maskDirty) {

            transformComponentCache = null;
            spineComponentCache = null;
            boneComponentCache = null;
            spriteComponentCache = null;
            particleComponentCache = null;
            routineRendererComponentCache = null;

            for (AComponent component : components) {
                Class<? extends AComponent> aClass = component.getClass();
                if (aClass == BoneComponent.class) {
                    componentBitMask |= BONE_COMPONENT_BIT;
                    boneComponentCache = (BoneComponent) component;
                } else if (aClass == CameraComponent.class) {
                    componentBitMask |= CAMERA_COMPONENT_BIT;
                } else if (aClass == CurveComponent.class) {
                    componentBitMask |= CURVE_COMPONENT_BIT;
                } else if (aClass == DataComponent.class) {
                    componentBitMask |= DATA_COMPONENT_BIT;
                } else if (aClass == EdgeCollider2DComponent.class) {
                    componentBitMask |= EDGE_COLLIDER_2D_COMPONENT_BIT;
                } else if (aClass == PaintSurfaceComponent.class) {
                    componentBitMask |= PAINT_SURFACE_COMPONENT_BIT;
                }else if (aClass == MapComponent.class) {
                    componentBitMask |= MAP_COMPONENT_BIT;
                } else if (aClass == ParticleComponent.class) {
                    componentBitMask |= PARTICLE_COMPONENT_BIT;
                    particleComponentCache = (ParticleComponent) component;
                } else if (aClass == PathRendererComponent.class) {
                    componentBitMask |= PATH_RENDERER_COMPONENT_BIT;
                } else if (aClass == RoutineRendererComponent.class) {
                    routineRendererComponentCache = (RoutineRendererComponent) component;
                    componentBitMask |= ROUTINE_RENDERER_COMPONENT_BIT;
                } else if (aClass == SpineRendererComponent.class) {
                    spineComponentCache = (SpineRendererComponent) component;
                    componentBitMask |= SPINE_RENDERER_COMPONENT_BIT;
                } else if (aClass == SpriteRendererComponent.class) {
                    spriteComponentCache = (SpriteRendererComponent) component;
                    componentBitMask |= SPRITE_RENDERER_COMPONENT_BIT;
                } else if (aClass == ScriptComponent.class) {
                    componentBitMask |= SCRIPT_COMPONENT_BIT;
                } else if (aClass == TileDataComponent.class) {
                    componentBitMask |= TILE_DATA_COMPONENT_BIT;
                } else if (aClass == TransformComponent.class) {
                    componentBitMask |= TRANSFORM_COMPONENT_BIT;
                    transformComponentCache = (TransformComponent) component;
                }

                if (aClass.isAssignableFrom(RendererComponent.class)) {
                    componentBitMask |= RENDERER_COMPONENT_BIT;
                }

            }
            maskDirty = false;
        }
    }

    public boolean hasTransformComponent () {
        //check bitmask
        recalculateBitMask();
        return (componentBitMask & TRANSFORM_COMPONENT_BIT) != 0;
    }

    public TransformComponent getTransformComponent () {
        recalculateBitMask();
        return transformComponentCache;
    }

    public boolean hasSpineComponent () {
        //check bitmask
        recalculateBitMask();
        return (componentBitMask & SPINE_RENDERER_COMPONENT_BIT) != 0;
    }

    public SpineRendererComponent getSpineComponent () {
        recalculateBitMask();
        return spineComponentCache;
    }

    public boolean hasBoneComponent ()  {
        //check bitmask
        recalculateBitMask();
        return (componentBitMask & BONE_COMPONENT_BIT) != 0;
    }
    public BoneComponent getBoneComponent () {
        recalculateBitMask();
        return boneComponentCache;
    }

    public boolean hasSpriteComponent () {
        //check bitmask
        recalculateBitMask();
        return (componentBitMask & SPRITE_RENDERER_COMPONENT_BIT) != 0;
    }

    public SpriteRendererComponent getSpriteComponent () {
        recalculateBitMask();
        return spriteComponentCache;
    }

    public boolean hasParticleComponent () {
        //check bitmask
        recalculateBitMask();
        return (componentBitMask & PARTICLE_COMPONENT_BIT) != 0;
    }

    public ParticleComponent getParticleComponent () {
        recalculateBitMask();
        return particleComponentCache;
    }

    public boolean hasRoutineRendererComponent () {
        //check bitmask
        recalculateBitMask();
        return (componentBitMask & ROUTINE_RENDERER_COMPONENT_BIT) != 0;
    }

    public RoutineRendererComponent getRoutineRendererComponent () {
        recalculateBitMask();
        return routineRendererComponentCache;
    }


    public GameObject () {
        uuid = UUID.randomUUID();
        routineEventListeners = new Array<>();
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
    public void write (Json json) {


        if (hasComponent(SpineRendererComponent.class)) {
            //deep collect and flatten of all things attached to bones

            //nested search gather all GO bound to these bones, and serialize
            SpineRendererComponent spineRendererComponent = getComponent(SpineRendererComponent.class);

            if (spineRendererComponent.getGameResource().isBroken()) {
                return;
            }

            Skeleton skeleton = spineRendererComponent.skeleton;
            Array<Bone> bones = skeleton.getBones();

            Array<GameObject> gosAttachedToBonesFromThisSkele = new Array<>();

            gatherAllChildrenAttachedToBones(this, bones, gosAttachedToBonesFromThisSkele);

            json.writeArrayStart("boneAttachedGOs");
            for (GameObject boneGO : gosAttachedToBonesFromThisSkele) {
                json.writeValue(boneGO, GameObject.class);
            }
            json.writeArrayEnd();

        }

        json.writeValue("name", name);
        json.writeValue("uuid", uuid.toString());
        json.writeValue("prefabLink", prefabLink);
        json.writeValue("active", active);
        json.writeValue("visible", editorVisible);
        json.writeValue("locked", editorTransformLocked);
        Bone attachedSpineBoneOrNull = getAttachedSpineBoneOrNull();
        if (attachedSpineBoneOrNull != null) {
            json.writeValue("parentBone", attachedSpineBoneOrNull.getData().getName());
        }

        json.writeArrayStart("components");
        for (AComponent component : components) {
            json.writeValue(component, AComponent.class);
        }
        json.writeArrayEnd();

        json.writeArrayStart("children");
        for (GameObject child : children) {
            if (child.hasComponent(BoneComponent.class)) { // special case, skip
                continue;
            }
            json.writeValue(child, GameObject.class);
        }
        json.writeArrayEnd();
    }

    public Bone getAttachedSpineBoneOrNull () {
        if (parent != null) {
            if (parent.hasBoneComponent()) {
                return parent.getBoneComponent().getBone();
            }
        }
        return null;
    }

    public static void gatherAllChildrenAttachedToBones(GameObject parent, Array<Bone> bones, Array<GameObject> out) {
        Bone attachedSpineBoneOrNull = parent.getAttachedSpineBoneOrNull();

        if (attachedSpineBoneOrNull != null) {
            if (bones.contains(attachedSpineBoneOrNull, true)) {

                if (!parent.hasBoneComponent()) {
                    out.add(parent);
                    return;
                }
            }
        }
        for (GameObject gameObject : parent.getGameObjects()) {
            gameObject.gatherAllChildrenAttachedToBones(gameObject, bones, out);
        }
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        name = jsonData.getString("name", null);
        if (name == null) {
            return;
        }
        setTalosIdentifier(GameResourceOwner.readTalosIdentifier(jsonData));

        if (jsonData.has("uuid")) {
            uuid = UUID.fromString(jsonData.getString("uuid"));
        } else {
            uuid = UUID.randomUUID();
        }
        prefabLink = jsonData.getString("prefabLink", null);
        active = jsonData.getBoolean("active", this.active);
        editorTransformLocked = jsonData.getBoolean("locked", this.editorTransformLocked);
        editorVisible = jsonData.getBoolean("visible", this.editorVisible);
        readBoneName = jsonData.getString("parentBone", null);

        JsonValue componentsJson = jsonData.get("components");
        for(JsonValue componentJson : componentsJson) {
            componentJson.addChild("talosIdentifier", new JsonValue(talosContextIdentifier));
            AComponent component = json.readValue(AComponent.class, componentJson);

            if (component instanceof SpineRendererComponent) {
                if (!((SpineRendererComponent) component).generateGameObjectBones) {
                    //Check if we have boneAttachedGOs
                    //If we do, we should set this flag to true
                    JsonValue childrenJson = jsonData.get("boneAttachedGOs");
                    if (childrenJson != null && childrenJson.size > 0) {
                        ((SpineRendererComponent) component).generateGameObjectBones = true;
                    }
                }
            }

            //This is the part where bones are created
            addComponent(component);
        }

        if (hasComponent(SpineRendererComponent.class)) {
            SpineRendererComponent component = getComponent(SpineRendererComponent.class);
            // re attach back the children of bones
            JsonValue childrenJson = jsonData.get("boneAttachedGOs");
            if(childrenJson != null) {
                for (JsonValue childJson : childrenJson) {
                    childJson.addChild("talosIdentifier", new JsonValue(talosContextIdentifier));
                    GameObject childObject = json.readValue(GameObject.class, childJson);
                    GameObject boneGo = component.getParentBoneGameObjectFor(childObject.readBoneName);
                    boneGo.addGameObject(childObject);
                }
            }
        }

        JsonValue childrenJson = jsonData.get("children");
        if(childrenJson != null) {
            for (JsonValue childJson : childrenJson) {
                try {
                    childJson.addChild("talosIdentifier", new JsonValue(talosContextIdentifier));
                    GameObject childObject = json.readValue(GameObject.class, childJson);
                    addGameObject(childObject);
                } catch (NullPointerException e) {
                    continue;
                }
            }
        }
    }

    @Override
    public void addGameObject (GameObject gameObject) {
        children.add(gameObject);
        childrenMap.put(gameObject.name, gameObject);

        gameObject.setParent(this);

        gameObjectDirty();
        getTopParent(null).hierarchyDirty = true;
    }

    @Override
    public  Array<GameObject> deleteGameObject (GameObject gameObject) {
        Array<GameObject> deletedGameObjects = new Array<>();

        String name = gameObject.getName();
        if (childrenMap.containsKey(name)) {
            GameObject objectToRemove = childrenMap.get(name);
            childrenMap.remove(name);
            children.removeValue(objectToRemove, true);
            tmp.add(objectToRemove);

            objectToRemove.clearChildren(tmp);
        }

        return deletedGameObjects;
    }

    @Override
    public void removeObject (GameObject gameObject) {
        String name = gameObject.getName();
        if (childrenMap.containsKey(name)) {
            childrenMap.remove(name);
        }
        children.removeValue(gameObject, true);

        gameObjectDirty();
        getTopParent(null).hierarchyDirty = true;
    }

    @Override
    public void clearChildren (Array<GameObject> tmp) {
        for (GameObject child : children) {
            child.clearChildren(tmp);
        }

        tmp.addAll(children);

        children.clear();
        childrenMap.clear();

        gameObjectDirty();
        getTopParent(null).hierarchyDirty = true;
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

        gameObjectDirty();
    }

    public void gameObjectDirty () {
        calculatedBoneChildren = false;
        maskDirty = true;
    }

    @Override
    public void removeComponent (AComponent component) {
        components.remove(component);
        componentClasses.remove(component.getClass());

        gameObjectDirty();
    }

    @Override
    public boolean hasGOWithName (String name) {
        if(childrenMap.containsKey(name)) return true;
        for(GameObject child: children) {
            if(child.hasGOWithName(name)) {
                return true;
            }
        }

        return false;
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

    private Array<GameObject> boneChildren;
    private boolean calculatedBoneChildren = false;
    public Array<GameObject> getChildrenWithBoneComponent () {
        if (!calculatedBoneChildren) {
            if (boneChildren == null) {
                boneChildren = new Array<>();
            }

            getChildrenByComponentFaster(BoneComponent.class, boneChildren);

            for (int i = boneChildren.size - 1; i >= 0; i--) {
                GameObject boneChildGO = boneChildren.get(i);

                if (!boneChildGO.active) {
                    boneChildren.removeIndex(i);
                    continue;
                }

                int amountOfChildren = boneChildGO.getGameObjects().size;

                if (amountOfChildren == 0) {
                    boneChildren.removeIndex(i);
                    continue;
                }

                if (amountOfChildren == 1) {
                    GameObject child = boneChildGO.getGameObjects().get(0);
                    //if it has a bone, we remove it, we do not care about single bone childs
                    if (child.hasBoneComponent()) {
                        boneChildren.removeIndex(i);
                        continue;
                    }
                }

                //otherwise iterate over all of these little fuckers and see if anyone has a child that isnt a single bone boi
                Array<GameObject> potentialInterestingGameObjects = boneChildGO.getGameObjects();

                boolean foundInteresting = false;
                for (GameObject potentialInterestingGameObject : potentialInterestingGameObjects) {

                    if (!potentialInterestingGameObject.active) {
                        continue;//not interesting
                    }

                    if (!potentialInterestingGameObject.hasBoneComponent()) {
                        foundInteresting = true;
                        break;
                    }
                }
                if (!foundInteresting) {
                    boneChildren.removeIndex(i);
                }

            }


            calculatedBoneChildren = true;
        }

        return boneChildren;
    }

    public Array<GameObject> getChildrenByComponentFaster (Class<?> clazz, Array<GameObject> list) {
        for(GameObject gameObject: children) {
            if(gameObject.hasComponent(clazz)) {
                list.add(gameObject);
            }
            if(gameObject.getGameObjects() != null && gameObject.getGameObjects().size > 0) {
                gameObject.getChildrenByComponentFaster(clazz, list);
            }
        }

        return list;
    }

    public Array<GameObject> getChildrenByComponent (Class<?> clazz, Array<GameObject> list) {
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

    public <T> T getComponentAssignableFrom (Class<? extends T> clazz) {
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
                ((RendererComponent) component).minMaxBounds(gameObject, minMax);
            }
        }
        for (GameObject child : gameObject.children) {
            estimateSizeForGameObject(child, minMax);
        }
    }
    public BoundingBox estimateSizeFromRoot () {
        boundingBox.clr();
        estimateSizeForGameObject(this, boundingBox);
        return boundingBox;
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
            if (parentToMoveTo.hasTransformComponent()) {
                parentPositionComponent = parentToMoveTo.getTransformComponent();
            }

            Vector2 tmp = TransformComponent.vec;
            tmp.set(childPositionComponent.worldPosition);
            tmp.sub(parentPositionComponent.worldPosition);
            childPositionComponent.position.set(tmp);
            childPositionComponent.rotation -= parentPositionComponent.rotation;
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

    public GameObject getChildByName (String name, boolean recursive) {
        for (int i = 0; i < children.size; i++) {
            GameObject child = children.get(i);
            if (child.name.equals(name)) {
                return child;
            }

            if (recursive) {
                if (child.getGameObjects() != null) {
                    GameObject childByName = child.getChildByName(name, recursive);
                    if (childByName != null) {
                        return childByName;
                    }
                }
            }
        }

        return null;
    }

    public GameObject getChildByUUID (UUID uuid) {
        for (int i = 0; i < children.size; i++) {
            GameObject child = children.get(i);
            if (child.uuid.equals(uuid)) {
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

    public void setEditorVisible(boolean editorVisible) {
        this.editorVisible = editorVisible;
        for (GameObject child : children) {
            child.setEditorVisible(editorVisible);
        }
    }

    public boolean isEditorVisible (){
        return editorVisible;
    }

    public void setEditorTransformLocked(boolean editorTransformLocked) {
        this.editorTransformLocked = editorTransformLocked;
        for (GameObject child : children) {
            child.setEditorTransformLocked(editorTransformLocked);
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

        for (GameObject child : children) {
            child.findGOsWithComponents(result, clazz);
        }

        return result;
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

    @Override
    public void onEventFromRoutines(String eventName, Array<PropertyWrapper<?>> propertyWrappers) {
        for (RoutineEventInterface routineEventListener : routineEventListeners) {
            routineEventListener.onEventFromRoutines(eventName, propertyWrappers);
        }
    }

    @Override
    public void addNodeEventListener(RoutineEventInterface routineEventInterface) {
        routineEventListeners.add(routineEventInterface);
    }

    @Override
    public String getTalosIdentifier () {
        return talosContextIdentifier;
    }

    @Override
    public void setTalosIdentifier (String identifier) {
        this.talosContextIdentifier = identifier;
    }
}
