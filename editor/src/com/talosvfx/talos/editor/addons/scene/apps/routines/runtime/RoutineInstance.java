package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.draw.DrawableQuad;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes.ExposedVariableNode;
import com.talosvfx.talos.editor.addons.scene.utils.propertyWrappers.*;
import com.talosvfx.talos.editor.data.RoutineStageData;
import lombok.Getter;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public class RoutineInstance {

    private static final Logger logger = LoggerFactory.getLogger(RoutineInstance.class);

    private Array<RoutineNode> nodes = new Array<>();


    private ObjectMap<String, RoutineNode> lookup = new ObjectMap<>();

    public IntMap<RoutineNode> lowLevelLookup = new IntMap<>();

    private RoutineConfigMap config;

    public Array<DrawableQuad> drawableQuads = new Array<>();

    public ObjectMap<String, Object> memory = new ObjectMap<>();

    public Array<Integer> scopeNumbers = new Array<>();
    private float requesterId;

    public transient boolean isDirty = true;

    @Getter
    private Array<PropertyWrapper<?>> parentPropertyWrappers;

    public RoutineInstance() {
        Pools.get(DrawableQuad.class, 100);
    }


    public void loadFrom (RoutineStageData routineStageData, RoutineConfigMap config) {
        this.config = config;
        this.isDirty = true;

        parentPropertyWrappers = routineStageData.getPropertyWrappers();

        JsonValue list = routineStageData.getJsonNodes();
        JsonValue connections = routineStageData.getJsonConnections();

        if (list == null || connections == null) {
            return;
        }



        IntMap<RoutineNode> idMap = new IntMap<>();

        String nodePackageName = "com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes.";
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
                e.printStackTrace();
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

        updateNodesFromProperties();
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

    public PropertyWrapper<?> getPropertyWrapperWithIndex (int index) {
        for (PropertyWrapper<?> propertyWrapper : parentPropertyWrappers) {
            if (propertyWrapper.index == index) {
                return propertyWrapper;
            }
        }

        return null;
    }

    public void updateNodesFromProperties () {
        for (IntMap.Entry<RoutineNode> routineNodeEntry : lowLevelLookup) {
            RoutineNode value = routineNodeEntry.value;
            if (value instanceof ExposedVariableNode) {
                ExposedVariableNode exposedVariableNode = (ExposedVariableNode) value;
                int index = exposedVariableNode.index;
                exposedVariableNode.updateForPropertyWrapper(getPropertyWrapperWithIndex(index));
            }
        }
    }
}
