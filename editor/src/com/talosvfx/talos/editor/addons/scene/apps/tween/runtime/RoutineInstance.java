package com.talosvfx.talos.editor.addons.scene.apps.tween.runtime;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.draw.DrawableQuad;

public class RoutineInstance {

    private Array<RoutineNode> nodes = new Array<>();

    private ObjectMap<String, RoutineNode> lookup = new ObjectMap<>();

    private RoutineConfigMap config;

    public Array<DrawableQuad> drawableQuads = new Array<>();

    public RoutineInstance() {
        Pools.get(DrawableQuad.class, 100);
    }

    public void loadFrom(String fileContent, RoutineConfigMap config) {
        this.config = config;

        JsonReader jsonReader = new JsonReader();
        JsonValue root = jsonReader.parse(fileContent);

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

                JsonValue properties = nodeData.get("properties");
                if(properties != null) {
                    if(properties.has("id")) {
                        lookup.put(properties.getString("id"), routineNode);
                    }
                }

                idMap.put(id, routineNode);
            } catch (ReflectionException e) {
                throw new RuntimeException(e);
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

    public XmlReader.Element getConfig(String name) {
        return config.getConfig(name);
    }

    public void clearMemory() {
        for(DrawableQuad quad: drawableQuads) {
            Pools.free(quad);
        }

        drawableQuads.clear();
    }
}
