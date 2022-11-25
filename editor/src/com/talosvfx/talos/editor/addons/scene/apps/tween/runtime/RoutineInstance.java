package com.talosvfx.talos.editor.addons.scene.apps.tween.runtime;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.draw.DrawableQuad;

import java.util.UUID;

public class RoutineInstance {

    private Array<RoutineNode> nodes = new Array<>();

    public transient UUID uuid;

    private ObjectMap<String, RoutineNode> lookup = new ObjectMap<>();

    private IntMap<RoutineNode> lowLevelLookup = new IntMap<>();

    private RoutineConfigMap config;

    public Array<DrawableQuad> drawableQuads = new Array<>();

    public Array<Integer> scopeNumbers = new Array<>();
    private float requesterId;

    public transient boolean isDirty = true;

    public RoutineInstance() {
        Pools.get(DrawableQuad.class, 100);
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

        IntMap<RoutineNode> idMap = new IntMap<>();

        for(JsonValue nodeData: list) {
            String nodeName = nodeData.getString("name");
            int id = nodeData.getInt("id");

            String packageName = "com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.nodes." + nodeName;
            Class clazz = null;
            try {
                clazz = ClassReflection.forName(packageName);
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

    public void beingDepth() {
        scopeNumbers.add(0);
    }

    public void incrementDepth() {
        scopeNumbers.set(scopeNumbers.size - 1, scopeNumbers.get(scopeNumbers.size - 1) + 1);
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
}