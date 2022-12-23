package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.GameAssetType;

public abstract class RoutineNode {

    private GameAsset.GameAssetUpdateListener updateListener;

    protected RoutineInstance routineInstanceRef;
    public int uniqueId;

    protected boolean nodeDirty = false;

    public enum DataType {
        NUMBER,
        VECTOR2,
        VECTOR3,
        COLOR,
        ASSET,
        STRING,
        FLUID
    }

    public enum PortType {
        INPUT,
        OUTPUT,
        NONE
    }

    public enum ConnectionType {
        SIGNAL,
        DATA
    }

    public class Connection {
        public Port fromPort;
        public Port toPort;
    }

    public class Port {
        public String name;
        public RoutineNode nodeRef;
        public PortType portType = PortType.NONE;
        public ConnectionType connectionType;
        public DataType dataType;
        public Array<Connection> connections = new Array<>();
        public Object valueOverride;

        public void setValueFromString(String val) {
            if(dataType == DataType.FLUID) {
                valueOverride = Float.parseFloat(val);
            } else if (dataType == DataType.NUMBER) {
                valueOverride = Float.parseFloat(val);
            } else {
                valueOverride = val;
            }
        }

        public void setValue(Object object) {
            valueOverride = object;
        }
    }

    protected ObjectMap<String, Port> inputs = new ObjectMap<>();
    protected ObjectMap<String, Port> outputs = new ObjectMap<>();

    public RoutineNode() {
        updateListener = new GameAsset.GameAssetUpdateListener() {
            @Override
            public void onUpdate () {
                RoutineNode.this.routineInstanceRef.isDirty = true;
                RoutineNode.this.nodeDirty = true;
            }
        };
    }

    public void loadFrom(RoutineInstance routineInstance, JsonValue nodeData) {
        routineInstanceRef = routineInstance;

        // load properties
        String name = nodeData.getString("name");
        XmlReader.Element config = routineInstanceRef.getConfig(name);
        constructNode(config);

        JsonValue properties = nodeData.get("properties");
        configureNode(properties);

        uniqueId = nodeData.getInt("id");
    }

    private void constructNode(XmlReader.Element config) {
        int rowCount = config.getChildCount();
        for (int i = 0; i < rowCount; i++) {
            XmlReader.Element row = config.getChild(i);
            processRow(row);
        }
    }

    private void processRow(XmlReader.Element row) {


        if(row.getName().equals("group")) {
            int rowCount = row.getChildCount();
            for (int i = 0; i < rowCount; i++) {
                processRow(row.getChild(i));
            }

            return;
        }

        String name = row.getAttribute("name");

        Port port = new Port();
        port.name = name;
        port.nodeRef = this;

        String portType = row.getAttribute("port", "");

        String type = row.getAttribute("type", "text");
        if(type.equals("signal")) {
            port.connectionType = ConnectionType.SIGNAL;
        } else {
            port.connectionType = ConnectionType.DATA;

            if(type.equals("int")) port.dataType = DataType.NUMBER;
            if(type.equals("float")) port.dataType = DataType.NUMBER;
            if(type.equals("vec2")) port.dataType = DataType.VECTOR2;
            if(type.equals("vec3")) port.dataType = DataType.VECTOR3;
            if(type.equals("color")) port.dataType = DataType.COLOR;
            if(type.equals("asset")) port.dataType = DataType.ASSET;
            if(type.equals("text")) port.dataType = DataType.STRING;
            if(type.equals("fluid")) port.dataType = DataType.FLUID;
        }

        if(portType.equals("input")) {
            port.portType = PortType.INPUT;
            inputs.put(name, port);
        } else if(portType.equals("output")) {
            port.portType = PortType.OUTPUT;
            outputs.put(name, port);
        } else {
            inputs.put(name, port);
        }
    }

    protected void configureNode(JsonValue properties) {
        for(JsonValue item: properties) {
            String name = item.name;

            Port port = inputs.get(name);
            JsonValue jsonValue = properties.get(name);
            if(port.dataType == DataType.COLOR) {
                Json json = new Json();
                Color color = json.readValue(Color.class, jsonValue);
                port.setValue(color);
            } else {
                port.setValueFromString(properties.getString(name));
            }
        }
    }


    public void addConnection(RoutineNode toNode, String fromSlot, String toSlot) {
        Port fromPort = outputs.get(fromSlot);
        Port toPort = toNode.getInputPort(toSlot);

        Connection leftConnection = new Connection();
        Connection rightConnection = new Connection();

        leftConnection.fromPort = fromPort;
        leftConnection.toPort = toPort;

        rightConnection.fromPort = toPort;
        rightConnection.toPort = fromPort;

        fromPort.connections.add(leftConnection);
        toPort.connections.add(rightConnection);
    }

    private Port getInputPort(String name) {
        return inputs.get(name);
    }

    /**
     * I just got signal to one of my ports
     * @param portName
     */
    public void receiveSignal(String portName) {

    }

    /**
     * Send signal to all connections from particular output port
     * @param portName
     */
    public void sendSignal(String portName) {
        Port port = outputs.get(portName);
        if(port != null) {
            if(port.connectionType == ConnectionType.SIGNAL) {
                for(Connection connection: port.connections) {
                    RoutineNode targetNode = connection.toPort.nodeRef;
                    String targetName = connection.toPort.name;

                    targetNode.receiveSignal(targetName);
                }
            }
        }
    }

    protected GameAsset fetchAssetValue(String key) {
        Port port = inputs.get(key);

        if(port.connections.isEmpty()) {
            if (port.valueOverride == null) {
                return null;
            }
            if (port.valueOverride instanceof GameAsset) {
                return (GameAsset) (port.valueOverride);
            } else {
                //todo: fix assumption that it is PNG
                GameAsset asset = AssetRepository.getInstance().getAssetForIdentifier((String) port.valueOverride, GameAssetType.SPRITE);
                if (!asset.listeners.contains(updateListener, true)) {
                    asset.listeners.add(updateListener);
                }
                return asset;
            }
        } else {
            Connection connection = port.connections.first();
            RoutineNode targetNode = connection.toPort.nodeRef;
            String targetPortName = connection.toPort.name;

            GameAsset gameAsset = (GameAsset) targetNode.queryValue(targetPortName);
            if (!gameAsset.listeners.contains(updateListener, true)) {
                gameAsset.listeners.add(updateListener);
            }

            return gameAsset;
        }
    }

    protected String fetchStringValue(String key) {
        Port port = inputs.get(key);



        return (String) port.valueOverride;
    }

    protected float fetchFloatValue(String key) {
        Object object = fetchValue(key);

        if(object instanceof Integer) {
            int result = (int) object;
            return (float) result;
        }

        return (float)object;
    }

    protected Color fetchColorValue(String key) {
        Object object = fetchValue(key);
        return (Color)object;
    }

    protected boolean fetchBooleanValue(String key) {
        Object object = fetchValue(key);
        if(object instanceof String) {
            boolean result = Boolean.parseBoolean((String) object);
            return result;
        }
        return (boolean)object;
    }

    protected int fetchIntValue(String key) {
        Object object = fetchValue(key);

        if(object instanceof Float) {
            float result = (float) object;
            return (int) Math.floor(result);
        }

        return (int)object;
    }

    protected Vector2 fetchVector2Value(String key) {
        Object object = fetchValue(key);

        return (Vector2)object;
    }

    /**
     * Ask my input port for it's value
     * @param key
     * @return
     */
    protected Object fetchValue(String key) {

        routineInstanceRef.setRequester(uniqueId);

        Port port = inputs.get(key);

        if(port == null) return null;

        if(port.connectionType == ConnectionType.DATA) {
            if(!port.connections.isEmpty()) {
                Connection connection = port.connections.first();
                RoutineNode targetNode = connection.toPort.nodeRef;
                String targetPortName = connection.toPort.name;

                return targetNode.queryValue(targetPortName);
            } else {
                if(port.valueOverride == null && port.dataType == DataType.NUMBER) {
                    return 0f;
                }
                return port.valueOverride;
            }
        }

        return null;
    }

    /**
     * I have been asked from my output port to provide its value
     * @param targetPortName
     */
    public Object queryValue(String targetPortName) {

        return 0;
    }

    public void setProperty(String key, Object value) {
        if(inputs.containsKey(key)) {
            inputs.get(key).valueOverride = value;
        }
    }

}
