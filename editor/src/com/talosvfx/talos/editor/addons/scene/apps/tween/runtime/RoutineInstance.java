package com.talosvfx.talos.editor.addons.scene.apps.tween.runtime;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.draw.DrawableQuad;
import com.talosvfx.talos.editor.addons.scene.utils.scriptProperties.PropertyFloatWrapper;
import com.talosvfx.talos.editor.addons.scene.utils.scriptProperties.PropertyWrapper;

import java.util.UUID;

public class RoutineInstance {

    private Array<RoutineNode> nodes = new Array<>();

    public transient UUID uuid;

    private ObjectMap<String, RoutineNode> lookup = new ObjectMap<>();

    private IntMap<RoutineNode> lowLevelLookup = new IntMap<>();

    private RoutineConfigMap config;

    public Array<DrawableQuad> drawableQuads = new Array<>();

    public ObjectMap<String, Object> memory = new ObjectMap<>();

    public Array<Integer> scopeNumbers = new Array<>();
    private float requesterId;

    public Array<PropertyWrapper<?>> getPropertyWrappers () {
        return propertyWrappers;
    }

    private int exposedPropertyIndex;

    private Array<PropertyWrapper<?>> propertyWrappers = new Array<>();

    public transient boolean isDirty = true;

    public RoutineInstance() {
        Pools.get(DrawableQuad.class, 100);
    }

    public int getExposedPropertyIndex() {
        return exposedPropertyIndex;
    }

    public void setExposedPropertyIndex(int index) {
        this.exposedPropertyIndex = index;
    }

    public void loadFrom(UUID uuid, String fileContent, RoutineConfigMap config) {
        this.config = config;
        this.uuid = new UUID(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
        this.isDirty = true;

        if(fileContent == null || fileContent.isEmpty()) return;

        JsonReader jsonReader = new JsonReader();
        JsonValue root = jsonReader.parse(fileContent);

        if(root == null) return;

        JsonValue list = root.get("list");
        JsonValue connections = root.get("connections");

        if (list == null || connections == null) {
            return;
        }

        propertyWrappers.clear();
        JsonValue propertiesJson = root.get("propertyWrappers");
        Json json = new Json();
        if (propertiesJson != null) {
            for (JsonValue propertyJson : propertiesJson) {
                String className = propertyJson.getString("className", "");
                JsonValue property = propertyJson.get("property");
                if (property != null) {
                    try {
                        Class clazz = ClassReflection.forName(className);
                        PropertyWrapper propertyWrapper = (PropertyWrapper) ClassReflection.newInstance(clazz);
                        propertyWrapper.read(json, property);
                        propertyWrappers.add(propertyWrapper);
                    } catch (ReflectionException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        exposedPropertyIndex = root.getInt("exposedPropertyIndex", 0);

        IntMap<RoutineNode> idMap = new IntMap<>();

        String nodePackageName = "com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.nodes.";
        for(JsonValue nodeData: list) {
            String nodeName = nodeData.getString("name");
            int id = nodeData.getInt("id");

            Class clazz = null;
            try {
                clazz = ClassReflection.forName(nodePackageName + nodeName);
                RoutineNode routineNode = (RoutineNode) ClassReflection.newInstance(clazz);
                routineNode.loadFrom(this, nodeData);
                lowLevelLookup.put(routineNode.uniqueId, routineNode);

                JsonValue properties = nodeData.get("properties");
                if(properties != null) {
                    if(properties.has("id")) {
                        lookup.put(properties.getString("id"), routineNode);
                    }
                }

                idMap.put(id, routineNode);
            } catch (ReflectionException e) {
                //throw new RuntimeException(e);
            }
        }


        for(JsonValue connectionJson: connections) {
            int fromId = connectionJson.getInt("fromNode");
            int toId = connectionJson.getInt("toNode");

            if(idMap.containsKey(fromId) && idMap.containsKey(toId)) {
                RoutineNode fromNode = idMap.get(fromId);
                RoutineNode toNode = idMap.get(toId);

                String fromSlot = connectionJson.getString("fromSlot");
                String toSlot = connectionJson.getString("toSlot");

                fromNode.addConnection(toNode, fromSlot, toSlot);
            }
        }
    }

    public RoutineNode getNodeById(String id) {
        return lookup.get(id);
    }

    public RoutineNode getNodeById(Integer id) {
        return lowLevelLookup.get(id);
    }

    public XmlReader.Element getConfig(String name) {
        return config.getConfig(name);
    }

    public void setRequester(float id) {
        requesterId = id;
    }

    public void beginDepth () {
        scopeNumbers.add(0);
    }

    public void setDepthValue(int val) {
        scopeNumbers.set(scopeNumbers.size - 1, val);
    }

    public void endDepth() {
        scopeNumbers.removeIndex(scopeNumbers.size - 1);
    }

    public void clearMemory() {
        for(DrawableQuad quad: drawableQuads) {
            Pools.free(quad);
        }

        drawableQuads.clear();
    }

    public float getRequesterId() {
        return requesterId;
    }

    public float getDepthHash() {
        int hash = scopeNumbers.hashCode();
        return hash;
    }

    public void storeMemory(String name, Object value) {
        memory.put(name, value);
    }

    public Object fetchMemory(String name) {
        return memory.get(name);
    }

    public void createNewPropertyWrapper () {
        PropertyFloatWrapper propertyFloatWrapper = new PropertyFloatWrapper();
        propertyFloatWrapper.index = exposedPropertyIndex;
        exposedPropertyIndex++;
        propertyWrappers.add(propertyFloatWrapper);
    }
}
