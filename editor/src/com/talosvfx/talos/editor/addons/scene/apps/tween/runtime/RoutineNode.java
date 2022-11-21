package com.talosvfx.talos.editor.addons.scene.apps.tween.runtime;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.sun.org.apache.bcel.internal.generic.FLOAD;

public abstract class RoutineNode {

    protected RoutineInstance routineInstanceRef;

    enum DataType {
        NUMBER,
        VECTOR2,
        VECTOR3,
        COLOR,
        ASSET,
        STRING
    }

    enum PortType {
        INPUT,
        OUTPUT,
        NONE
    }

    enum ConnectionType {
        SIGNAL,
        DATA
    }

    public class Connection {
        Port fromPort;
        Port toPort;
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
            if(dataType == DataType.NUMBER) {
                valueOverride = Float.parseFloat(val);
            } else {
                valueOverride = val;
            }
        }
    }

    private ObjectMap<String, Port> inputs = new ObjectMap<>();
    private ObjectMap<String, Port> outputs = new ObjectMap<>();

    public RoutineNode() {

    }

    public void loadFrom(RoutineInstance routineInstance, JsonValue nodeData) {
        routineInstanceRef = routineInstance;

        // load properties
        String name = nodeData.getString("name");
        XmlReader.Element config = routineInstanceRef.getConfig(name);
        constructNode(config);

        JsonValue properties = nodeData.get("properties");
        configureNode(properties);
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
            port.setValueFromString(properties.getString(name));
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
    public abstract void receiveSignal(String portName);

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

    /**
     * Ask my input port for it's value
     * @param key
     * @return
     */
    protected Object fetchValue(String key) {

        Port port = inputs.get(key);

        if(port == null) return null;

        if(port.connectionType == ConnectionType.DATA) {
            if(!port.connections.isEmpty()) {
                Connection connection = port.connections.first();
                RoutineNode targetNode = connection.toPort.nodeRef;
                String targetPortName = connection.toPort.name;

                targetNode.queryValue(targetPortName);

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
    public void queryValue(String targetPortName) {

    }

}
