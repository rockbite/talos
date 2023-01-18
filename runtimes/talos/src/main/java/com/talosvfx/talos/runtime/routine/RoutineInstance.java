package com.talosvfx.talos.runtime.routine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.talosvfx.talos.runtime.routine.draw.DrawableQuad;
import com.talosvfx.talos.runtime.routine.serialization.BaseRoutineData;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.SavableContainer;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyWrapper;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public class RoutineInstance {

    private static final Logger logger = LoggerFactory.getLogger(RoutineInstance.class);

    @Getter
    private ObjectMap<String, RoutineNode> customLookup = new ObjectMap<>();

    private ObjectMap<String, RoutineNode> lookup = new ObjectMap<>();

    @Getter
    private ObjectMap<String, PropertyWrapper> properties = new ObjectMap<>();

    private Array<TickableNode> tickableNodes = new Array<>();

    public IntMap<RoutineNode> lowLevelLookup = new IntMap<>();

    private RoutineConfigMap config;

    public Array<DrawableQuad> drawableQuads = new Array<>();

    public ObjectMap<String, Object> memory = new ObjectMap<>();

    public ObjectMap<String, Object> globalMap = new ObjectMap<>();

    public Array<Integer> scopeNumbers = new Array<>();
    private float requesterId;

    @Getter
    private transient boolean isDirty = true;

    @Getter@Setter
    private float timeScale = 1f;

    public boolean configured = false;

    @Getter
    private Array<PropertyWrapper<?>> parentPropertyWrappers;

    @Getter
    private Object signalPayload;

    @Setter
    private RoutineListenerAdapter listener;

    @Getter@Setter
    private SavableContainer container;
    private GameObject cameraGO;
    private boolean paused = false;

    public void reset() {
        clearMemory();
        globalMap.clear();
        scopeNumbers.clear();
        timeScale = 1f;

        signalPayload = null;

        for (IntMap.Entry<RoutineNode> routineNodeEntry : lowLevelLookup) {
            routineNodeEntry.value.reset();
        }
    }

    public <T extends RoutineNode> Array<T> getNodesByClass(Class<T> clazz) {
        Array<T> result = new Array<>();
        for (IntMap.Entry<RoutineNode> entry : lowLevelLookup) {
            if(entry.value.getClass().isAssignableFrom(clazz)) {
                result.add((T) entry.value);
            }
        }

        return result;
    }

    public void complete() {
        if(listener != null) {
            if(!listener.isTerminated()) {
                listener.onComplete();
            }
        }
    }

    public void removeListener() {
        listener = null;
    }

    public void setDirty() {
        configured = false;
        this.isDirty = true;
    }

    public void setDirty(boolean dirty) {
        this.isDirty = dirty;
    }

    public void setCameraGO(GameObject cameraGO) {
        this.cameraGO = cameraGO;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public static class RoutineListenerAdapter implements RoutineListener {

        @Getter
        private boolean terminated = false;

        @Override
        public void onSignalSent(int nodeId, String port) {

        }

        @Override
        public void onInputFetched(int nodeId, String port) {

        }

        @Override
        public void onComplete() {

        }

        public void terminate() {
            terminated = true;
        }
    }

    public interface RoutineListener {
        void onSignalSent(int nodeId, String port);

        void onInputFetched(int nodeId, String port);

        void onComplete();
    }

    public RoutineInstance() {
        Pools.get(DrawableQuad.class, 100);
    }


    public void loadFrom (BaseRoutineData routineStageData, RoutineConfigMap config) {
        this.config = config;
        this.isDirty = true;

        parentPropertyWrappers = routineStageData.getPropertyWrappers();

        JsonValue list = routineStageData.getJsonNodes();
        JsonValue connections = routineStageData.getJsonConnections();

        if (list == null || connections == null) {
            return;
        }

        IntMap<RoutineNode> idMap = new IntMap<>();

        String nodePackageName = "com.talosvfx.talos.runtime.routine.nodes.";
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

                if(routineNode instanceof TickableNode) {
                    tickableNodes.add((TickableNode) routineNode);
                }
            } catch (ReflectionException e) {
                e.printStackTrace();
            }
        }

        configureConnections(connections, idMap);
    }

    private void configureConnections(JsonValue connections, IntMap<RoutineNode> idMap) {
        boolean configured = checkConfigured();
        if(configured) {
            for (JsonValue connectionJson : connections) {
                int fromId = connectionJson.getInt("fromNode");
                int toId = connectionJson.getInt("toNode");

                if (idMap.containsKey(fromId) && idMap.containsKey(toId)) {
                    RoutineNode fromNode = idMap.get(fromId);
                    RoutineNode toNode = idMap.get(toId);

                    String fromSlot = connectionJson.getString("fromSlot");
                    String toSlot = connectionJson.getString("toSlot");

                    fromNode.addConnection(toNode, fromSlot, toSlot);
                }
            }
        } else {
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    configureConnections(connections, idMap);
                }
            });
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

    public void storeGlobal(String name, Object value) {
        globalMap.put(name, value);
    }

    public Object fetchGlobal(String name) {
        return globalMap.get(name);
    }

    public PropertyWrapper<?> getPropertyWrapperWithIndex (int index) {
        for (PropertyWrapper<?> propertyWrapper : parentPropertyWrappers) {
            if (propertyWrapper.index == index) {
                return propertyWrapper;
            }
        }

        return null;
    }

    public void setSignalPayload(Object payload) {
        this.signalPayload = payload;
    }

    public void tick(float delta) {
        if(checkConfigured()) {
            if(!paused) {
                for (TickableNode node : tickableNodes) {
                    node.tick(delta * timeScale);
                }
            }
        }
    }

    public void onSignalSent(int nodeId, String portName) {
        if(listener != null) {
            listener.onSignalSent(nodeId, portName);
        }
    }


    public void onInputFetched(int nodeId, String portName) {
        if(listener != null) {
            listener.onInputFetched(nodeId, portName);
        }
    }

    public boolean checkConfigured() {
        if(!configured) {
            for (IntMap.Entry<RoutineNode> entry : lowLevelLookup) {
                if(!entry.value.isConfigured()) {
                    return false;
                }
            }

            configured = true;
        }

        return true;
    }

    public void applyQuadDiff(Vector2 diff) {
        for (DrawableQuad drawableQuad : drawableQuads) {
            drawableQuad.position.add(diff);
        }
    }

    public void stop() {
        reset();
    }
}
