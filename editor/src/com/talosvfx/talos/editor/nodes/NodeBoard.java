package com.talosvfx.talos.editor.nodes;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.rockbite.bongo.engine.render.ShaderFlags;
import com.rockbite.bongo.engine.render.ShaderSourceProvider;
import com.rockbite.bongo.engine.render.SpriteShaderCompiler;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.Curve;
import com.talosvfx.talos.editor.addons.shader.nodes.ColorOutput;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.events.NodeConnectionCreatedEvent;
import com.talosvfx.talos.editor.notifications.events.NodeConnectionRemovedEvent;
import com.talosvfx.talos.editor.notifications.events.NodeDataModifiedEvent;
import com.talosvfx.talos.editor.notifications.events.NodeRemovedEvent;
import com.talosvfx.talos.runtime.Slot;
import com.talosvfx.talos.runtime.modules.AbstractModule;

public class NodeBoard extends WidgetGroup implements Notifications.Observer {

    private Skin skin;

    ShapeRenderer shapeRenderer;
    private Curve activeCurve;
    private Bezier<Vector2> bezier = new Bezier<>();
    private Vector2[] curvePoints = new Vector2[4];
    Vector2 tmp = new Vector2();
    Vector2 tmp2 = new Vector2();
    Vector2 prev = new Vector2();

    public int globalNodeCounter = 0;
    private ObjectIntMap<Class<? extends NodeWidget>> nodeCounter = new ObjectIntMap<>();

    private ObjectSet<NodeWidget> selectedNodes = new ObjectSet<>();

    private DynamicNodeStage nodeStage;

    private NodeWidget ccFromNode = null;
    private String ccFromSlot = null;
    private boolean ccCurrentIsInput = false;
    public boolean ccCurrentlyRemoving = false;
    private NodeWidget wasNodeSelectedOnDown = null;
    private NodeWidget wasNodeDragged = null;

    public Array<NodeConnection> nodeConnections = new Array<>();
    public Array<NodeWidget> nodes = new Array<>();

    public Array<NodeGroup> groups = new Array<>();
    public Group groupContainer = new Group();
    public Group mainContainer = new Group();

    public void reset () {
        nodeCounter = new ObjectIntMap<>();
        selectedNodes.clear();
        nodeConnections.clear();
        nodes.clear();

        groups.clear();

        mainContainer.clearChildren();
        groupContainer.clearChildren();


    }

    public static class NodeConnection {
        public NodeWidget fromNode;
        public NodeWidget toNode;
        public String fromId;
        public String toId;
    }

    public NodeBoard(Skin skin, DynamicNodeStage nodeStage) {
        this.skin = skin;
        String shapeVertexSource = ShaderSourceProvider.resolveVertex("core/shape", Files.FileType.Classpath).readString();
        String shapeFragmentSource = ShaderSourceProvider.resolveFragment("core/shape", Files.FileType.Classpath).readString();

        shapeRenderer = new ShapeRenderer(5000,
            SpriteShaderCompiler.getOrCreateShader("core/shape", shapeVertexSource, shapeFragmentSource, new ShaderFlags())
        );

        this.nodeStage = nodeStage;

        curvePoints[0] = new Vector2();
        curvePoints[1] = new Vector2();
        curvePoints[2] = new Vector2();
        curvePoints[3] = new Vector2();

        addListener(new ClickListener() {

            @Override
            public boolean keyUp(InputEvent event, int keycode) {
                if(event.isHandled()) return super.keyUp(event, keycode);
                if(keycode == Input.Keys.DEL || keycode == Input.Keys.FORWARD_DEL) {
                    deleteSelectedNodes();
                }
                return super.keyUp(event, keycode);
            }
        });

        Notifications.registerObserver(this);

        addActor(groupContainer);
        addActor(mainContainer);
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
        batch.end();
        shapeRenderer.setProjectionMatrix(getStage().getCamera().combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawCurves();
        shapeRenderer.end();
        batch.begin();

        super.draw(batch, parentAlpha);
    }

    private void drawCurves() {
        // draw active curve
        if(activeCurve != null) {
            shapeRenderer.setColor(0, 203/255f, 124/255f, 1f);
            drawCurve(activeCurve.getFrom().x, activeCurve.getFrom().y, activeCurve.getTo().x, activeCurve.getTo().y);
        }

        shapeRenderer.setColor(1, 1, 1, 0.4f);
        // draw nodes
        for(NodeConnection connection: nodeConnections) {
            connection.fromNode.getOutputSlotPos(connection.fromId, tmp);
            float x = tmp.x;
            float y = tmp.y;
            connection.toNode.getInputSlotPos(connection.toId, tmp);
            float toX = tmp.x;
            float toY = tmp.y;
            drawCurve(x, y, toX, toY);
        }
    }

    private void drawCurve(float x, float y, float toX, float toY) {
        float minOffset = 10f;
        float maxOffset = 150f;

        float deltaX = Math.abs(toX - x);
        if(deltaX > maxOffset) deltaX = maxOffset;
        deltaX = deltaX/maxOffset;

        float offset = minOffset + (maxOffset-minOffset) * deltaX;

        curvePoints[0].set(x, y);
        curvePoints[1].set(x+offset, y);
        curvePoints[2].set(toX - offset, toY);
        curvePoints[3].set(toX + 20f, toY);

        bezier.set(curvePoints, 0, curvePoints.length);

        float resolution = 1f/20f;

        for(float i = 0; i < 1f; i+=resolution) {
            bezier.valueAt(tmp, i);
            if(i > 0) {
                shapeRenderer.rectLine(prev.x, prev.y, tmp.x, tmp.y, 2f);
            }
            prev.set(tmp);
        }
    }

    public NodeWidget createNode (Class<? extends NodeWidget> clazz, XmlReader.Element config, float x, float y) {
        NodeWidget node = null;
        try {
            tmp2.set(x, y);
            stageToLocalCoordinates(tmp2);

            node = ClassReflection.newInstance(clazz);
            node.init(skin, this);
            node.setConfig(config);

            mainContainer.addActor(node);

            node.setPosition(tmp2.x - node.getWidth()/2f, tmp2.y - node.getHeight()/2f);

            nodes.add(node);

            int counter = nodeCounter.getAndIncrement(clazz, 0, 1);
            node.setId(counter);
            node.setUniqueId(globalNodeCounter++);
        } catch (ReflectionException e) {
            e.printStackTrace();
        }

        TalosMain.Instance().ProjectController().setDirty();

        return node;
    }

    public void deleteSelectedNodes () {
        try {
            for (NodeWidget node : selectedNodes) {
                deleteNode(node);
            }
        } catch (Exception e) {
            TalosMain.Instance().reportException(e);
        }

        clearSelection();
    }

    public void deleteNode(NodeWidget node) {
        nodes.removeValue(node, true);

        for(int i = nodeConnections.size-1; i >= 0; i--) {
            if(nodeConnections.get(i).toNode == node || nodeConnections.get(i).fromNode == node) {
                removeConnection(nodeConnections.get(i));
            }
        }

        Notifications.fireEvent(Notifications.obtainEvent(NodeRemovedEvent.class).set(node));

        mainContainer.removeActor(node);

        TalosMain.Instance().ProjectController().setDirty();
    }

    public <T extends AbstractModule> void tryAndConnectLasCC(NodeWidget nodeWidget) {
        if(ccFromNode != null) {
            Class fromClass;
            Slot fromSlotObject;
            Array<String> toSlots;
            NodeWidget fromModule;
            NodeWidget toModule;
            String fromSlot = null;
            String toSlot = null;
            if(ccCurrentIsInput) {
                toSlots = nodeWidget.getOutputSlots();

                fromModule = nodeWidget;
                toModule = ccFromNode;
                toSlot = ccFromSlot;

            } else {
                toSlots = nodeWidget.getInputSlots();

                fromModule = ccFromNode;
                toModule = nodeWidget;
                fromSlot = ccFromSlot;
            }

            for(int i = 0; i < toSlots.size; i++) {
                String slot = toSlots.get(i);
                // we can connect
                if(ccCurrentIsInput) {
                    fromSlot = slot;
                } else {
                    toSlot = slot;
                }

                makeConnection(fromModule, toModule, fromSlot, toSlot);
                break;
            }

            ccFromNode = null;
        }
    }


    public NodeConnection findConnection(NodeWidget node, boolean isInput, String key) {
        NodeConnection nodeToFind =  null;
        for(NodeConnection nodeConnection: nodeConnections) {
            if((isInput && nodeConnection.toId.equals(key) && node == nodeConnection.toNode) ||
                    (!isInput && nodeConnection.toId.equals(key) && node == nodeConnection.fromNode)) {
                // found the node let's remove it
                nodeToFind = nodeConnection;
            }
        }

        return nodeToFind;
    }

    public void removeConnection (NodeConnection connection) {
        //Notifications.fireEvent(Notifications.obtainEvent(NodeConnectionPreRemovedEvent.class).set(connection));
        nodeConnections.removeValue(connection, true);

        connection.fromNode.setSlotInactive(connection.fromId, false);
        connection.toNode.setSlotInactive(connection.toId, true);

        Notifications.fireEvent(Notifications.obtainEvent(NodeConnectionRemovedEvent.class).set(connection));

        TalosMain.Instance().ProjectController().setDirty();
    }

    public void setActiveCurve(float x, float y, float toX, float toY, boolean isInput) {
        activeCurve = new Curve(x, y, toX, toY, isInput);
    }

    public void updateActiveCurve(float toX, float toY) {
        if(activeCurve != null) {
            activeCurve.setTo(toX, toY);
        }
    }

    public NodeConnection addConnectionCurve(NodeWidget from, NodeWidget to, String slotForm, String slotTo) {
        NodeConnection connection = new NodeConnection();
        connection.fromNode = from;
        connection.toNode = to;
        connection.fromId = slotForm;
        connection.toId = slotTo;

        nodeConnections.add(connection);

        from.setSlotActive(slotForm, false);
        to.setSlotActive(slotTo, true);

        return connection;
    }

    public void connectNodeIfCan(NodeWidget currentNode, String currentSlot, boolean currentIsInput) {
        Object[] result = new Object[2];
        NodeWidget target = null;
        boolean targetIsInput = false;
        // iterate over all widgets that are not current and see if mouse is over any of their slots, need to only connect input to output or output to input
        for(NodeWidget node: getNodes()) {
            if(node != currentNode) {
                node.findHoveredSlot(result);

                if((String)result[0] != null ) {
                    // found match
                    target = node;
                    if((int)result[1] == 0) {
                        targetIsInput = true;
                    } else {
                        targetIsInput = false;
                    }
                    break;
                }
            }
        }

        ccFromNode = null;

        if(target == null || currentIsInput == targetIsInput) {
            // removing
            // show popup (but maybe not in case of removing of existing curve)
            if(activeCurve.getFrom().dst(activeCurve.getTo()) > 20 && !ccCurrentlyRemoving) {
                final Vector2 vec = new Vector2(Gdx.input.getX(), Gdx.input.getY());
                (TalosMain.Instance().UIStage().getStage().getViewport()).unproject(vec);
                ccFromNode = currentNode;
                ccFromSlot = currentSlot;
                ccCurrentIsInput = currentIsInput;

                nodeStage.showPopup();
            }
        } else {
            // yay we are connecting
            NodeWidget fromWrapper, toWrapper;
            String fromSlot, toSlot;

            if(targetIsInput) {
                fromWrapper = currentNode;
                toWrapper = target;
                fromSlot = currentSlot;
                toSlot = (String)result[0];
            } else {
                fromWrapper = target;
                toWrapper = currentNode;
                fromSlot = (String)result[0];
                toSlot = currentSlot;
            }

            //what if this already exists?
            if(findConnection(toWrapper, true, toSlot) == null) {
                makeConnection(fromWrapper, toWrapper, fromSlot, toSlot);
            }
        }
        removeActiveCurve();
    }

    public void removeActiveCurve() {
        activeCurve = null;
    }

    public Array<NodeWidget> getNodes() {
        return nodes;
    }

    public void makeConnection(NodeWidget from, NodeWidget to, String slotFrom, String slotTo) {
        NodeConnection connection = addConnectionCurve(from, to, slotFrom, slotTo);

        from.attachNodeToMyOutput(to, slotFrom, slotTo);
        to.attachNodeToMyInput(from, slotTo, slotFrom);

        TalosMain.Instance().ProjectController().setDirty();

        Notifications.fireEvent(Notifications.obtainEvent(NodeConnectionCreatedEvent.class).set(connection));
    }

    /**
     * Selection
     */

    public void selectNode(NodeWidget node) {
        clearSelection();
        addNodeToSelection(node);
    }

    public void addNodeToSelection(NodeWidget node) {
        selectedNodes.add(node);
        updateSelectionBackgrounds();
    }

    public void removeNodeFromSelection(NodeWidget node) {
        selectedNodes.remove(node);
        updateSelectionBackgrounds();
    }

    public ObjectSet<NodeWidget> getSelectedNodes() {
        return selectedNodes;
    }

    public void setSelectedNodes(ObjectSet<NodeWidget> nodes) {
        selectedNodes.clear();
        selectedNodes.addAll(nodes);
        updateSelectionBackgrounds();
    }

    public void clearSelection() {
        selectedNodes.clear();
        updateSelectionBackgrounds();
    }

    public void updateSelectionBackgrounds() {
        for(NodeWidget wrapper : nodes) {
            if(getSelectedNodes().contains(wrapper)) {
                wrapper.setSelected(true);
            } else {
                wrapper.setSelected(false);
            }
        }
    }

    public void selectAllNodes() {
        ObjectSet<NodeWidget> nodes = new ObjectSet<>();
        for(NodeWidget node: getNodes()) {
            nodes.add(node);
        }
        setSelectedNodes(nodes);
    }

    private Array<NodeConnection> getSelectedConnections() {
        Array<NodeConnection> arr = new Array<>();
        ObjectSet<NodeWidget> nodes = getSelectedNodes();
        Array<NodeConnection> connections = nodeConnections;
        for(NodeConnection connection: connections) {
            if(nodes.contains(connection.fromNode) && nodes.contains(connection.toNode)) {
                arr.add(connection);
            }
        }

        return arr;
    }

    public static class ClipboardPayload implements Json.Serializable {
        Array<NodeConnection> connections;
        ObjectSet<NodeWidget> nodes;
        Array<NodeGroup> groups;

        Array<JsonValue> nodeJsonArray = new Array<>();
        Array<JsonValue> connectionsJsonArray = new Array<>();
        Array<JsonValue> groupJsonArray = new Array<>();

        public Vector2 cameraPositionAtCopy = new Vector2();

        public ClipboardPayload() {

        }

        public void set(ObjectSet<NodeWidget> nodes, Array<NodeConnection> connections, Array<NodeGroup> groups) {
            this.nodes = nodes;
            this.connections = connections;
            this.groups = groups;
        }

        @Override
        public void write(Json json) {
            json.writeArrayStart("nodes");
            for (NodeWidget node: nodes) {
                json.writeValue(node);
            }
            json.writeArrayEnd();

            json.writeValue("cameraPositionAtCopy", cameraPositionAtCopy);

            json.writeArrayStart("connections");
            for (NodeBoard.NodeConnection connection: connections) {
                json.writeObjectStart();
                json.writeValue("fromNode", connection.fromNode.getUniqueId());
                json.writeValue("toNode", connection.toNode.getUniqueId());
                json.writeValue("fromSlot", connection.fromId);
                json.writeValue("toSlot", connection.toId);
                json.writeObjectEnd();
            }
            json.writeArrayEnd();

            json.writeArrayStart("groups");
            for (NodeGroup group: groups) {
                json.writeObjectStart();
                json.writeValue("name", group.getText());
                json.writeValue("color", group.getFrameColor());
                json.writeArrayStart("nodes");

                for (NodeWidget nodeWidget: group.getNodes()) {
                    json.writeValue(nodeWidget.getUniqueId());
                }

                json.writeArrayEnd();
                json.writeObjectEnd();
            }
            json.writeArrayEnd();
        }

        @Override
        public void read(Json json, JsonValue jsonData) {
            JsonValue nodes = jsonData.get("nodes");
            nodeJsonArray.clear();
            connectionsJsonArray.clear();
            for (JsonValue nodeData: nodes) {
                nodeJsonArray.add(nodeData);
            }
            for (JsonValue connectionData: jsonData.get("connections")) {
                connectionsJsonArray.add(connectionData);
            }
            for (JsonValue groupData: jsonData.get("groups")) {
                groupJsonArray.add(groupData);
            }

            cameraPositionAtCopy = json.readValue("cameraPositionAtCopy", Vector2.class, jsonData);
        }
    }

    public void copySelectedModules() {
        Array<NodeConnection> connections = getSelectedConnections();
        ObjectSet<NodeWidget> nodes = getSelectedNodes();
        Array<NodeGroup> groups = getSelectedGroups();

        ClipboardPayload payload = new ClipboardPayload();
        payload.set(nodes, connections, groups);
        Vector3 camPos = getStage().getCamera().position;
        payload.cameraPositionAtCopy.set(camPos.x, camPos.y);

        Json json = new Json();
        String clipboard = json.toJson(payload);
        Gdx.app.getClipboard().setContents(clipboard);
    }

    public void pasteFromClipboard() {
        String clipboard = Gdx.app.getClipboard().getContents();

        ObjectMap<Integer, NodeWidget> previousNodeIdMap = new ObjectMap<>();

        boolean hasShaderModule = false;

        for (NodeWidget node: getNodes()) {
            if (node instanceof ColorOutput) hasShaderModule = true;
        }

        Json json = new Json();
        try {
            ClipboardPayload payload = json.fromJson(ClipboardPayload.class, clipboard);

            Vector3 camPosAtPaste = getStage().getCamera().position;
            Vector2 offset = new Vector2(camPosAtPaste.x, camPosAtPaste.y);
            offset.sub(payload.cameraPositionAtCopy);

            Array<JsonValue> nodeDataArray = payload.nodeJsonArray;

            ObjectSet<NodeWidget> copiedNodes = new ObjectSet<>();

            for(JsonValue nodeData: nodeDataArray) {
                String moduleName = nodeData.getString("name");
                Class clazz = nodeStage.getNodeListPopup().getNodeClassByName(moduleName);

                if(clazz == null || (clazz.equals(ColorOutput.class) && hasShaderModule)) {
                    continue;
                }

                NodeWidget node = createNode(clazz, nodeStage.getNodeListPopup().getModuleByName(moduleName), 0, 0);
                node.constructNode(nodeStage.getNodeListPopup().getModuleByName(moduleName));
                int uniqueId = node.getUniqueId();
                node.read(json, nodeData);
                node.setUniqueId(uniqueId);

                node.moveBy(offset.x, offset.y);

                previousNodeIdMap.put(nodeData.getInt("id"), node); // get old Id
                copiedNodes.add(node);
            }


            // now let's connect the connections
            for(JsonValue connectionData: payload.connectionsJsonArray) {
                int fromNodeId = connectionData.getInt("fromNode");
                int toNodeId = connectionData.getInt("toNode");
                String fromSlot = connectionData.getString("fromSlot");
                String toSlot = connectionData.getString("toSlot");

                NodeWidget fromNode = previousNodeIdMap.get(fromNodeId);
                NodeWidget toNode = previousNodeIdMap.get(toNodeId);
                if(fromNode == null || toNode == null) {
                    continue;
                }
                makeConnection(fromNode, toNode, fromSlot, toSlot);
            }

            // now let's add groups
            ObjectSet<NodeWidget> subNodeList = new ObjectSet<>();

            for(JsonValue groupData: payload.groupJsonArray) {
                String name = groupData.getString("name");
                Color color = json.readValue(Color.class, groupData.get("color"));
                JsonValue childNodeIds = groupData.get("nodes");
                subNodeList.clear();
                for (JsonValue idVal : childNodeIds) {
                    int id = idVal.asInt();
                    subNodeList.add(previousNodeIdMap.get(id));
                }

                NodeGroup nodeGroup = createGroupForNodes(subNodeList);
                nodeGroup.setText(name);
                nodeGroup.setColor(color);
            }

            setSelectedNodes(copiedNodes);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void nodeClicked(NodeWidget node) {
        wasNodeDragged = null;
        if(selectedNodes.contains(node)) {
            wasNodeSelectedOnDown = node;
        } else {
            wasNodeSelectedOnDown = null;
        }

        if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            addNodeToSelection(node);
        } else {
            if(!selectedNodes.contains(node)) {
                selectNode(node);
            }
        }
    }

    public void wrapperMovedBy(NodeWidget node, float x, float y) {
        wasNodeDragged = node;
        if(selectedNodes.size > 1) {
            for(NodeWidget other: selectedNodes) {
                if(other != node) {
                    other.moveBy(x, y);
                }
            }
        }
    }

    public void nodeClickedUp(NodeWidget node) {

        if(wasNodeDragged != null) {
            TalosMain.Instance().ProjectController().setDirty();
        } else {
            // on mouse up when no drag happens this wrapper should be selected unless shift was pressed
            if(!Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                selectNode(node);
            } else {
                if(wasNodeSelectedOnDown == node) {
                    removeNodeFromSelection(node);
                } else {
                    addNodeToSelection(node);
                }
            }
        }
    }

    @EventHandler
    public void onNodeDataModifiedEvent(NodeDataModifiedEvent event) {
        NodeWidget node = event.getNode();
        Array<NodeWidget> affectedNodes = new Array<>();
        collectNodesNodeAffects(affectedNodes, node);
        affectedNodes.removeValue(node, true);

        for(NodeWidget affectedNode: affectedNodes) {
            affectedNode.graphUpdated(); //TODO: this is not currently used but should be for more optimal stuff
        }
    }

    @EventHandler
    public void onNodeConnectionCreated(NodeConnectionCreatedEvent event) {

        // need to find affected node list
        Array<NodeWidget> affectedNodes = new Array<>();
        collectNodesNodeAffects(affectedNodes, event.getConnection().toNode);

        for(NodeWidget node: affectedNodes) {
            node.graphUpdated();
        }
    }

    @EventHandler
    public void onNodeConnectionRemoved(NodeConnectionRemovedEvent event) {
        // need to find affected node list
        Array<NodeWidget> affectedNodes = new Array<>();
        collectNodesNodeAffects(affectedNodes, event.getConnection().toNode);


        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run () {
                for(NodeWidget node: affectedNodes) {
                    node.graphUpdated();
                }
            }
        });
    }

    private void collectNodesNodeAffects(Array<NodeWidget> nodeList, NodeWidget node) {
        nodeList.add(node);

        for(NodeWidget.Connection connection: node.outputs.values()) {
            collectNodesNodeAffects(nodeList, connection.targetNode);
        }
    }

    public NodeGroup createGroupForNodes(ObjectSet<NodeWidget> nodes) {
        if(nodes == null || nodes.size == 0) return null;

        for(NodeGroup other: groups) {
            other.removeWrappers(nodes);
        }

        NodeGroup group = new NodeGroup(this, skin);
        group.setNodes(nodes);
        groups.add(group);

        groupContainer.addActor(group);

        TalosMain.Instance().ProjectController().setDirty();

        clearSelection();

        return group;
    }

    public void createGroupFromSelectedNodes () {
        createGroupForNodes(getSelectedNodes());
    }

    public void ungroupSelectedNodes() {
        ungroupNodes(getSelectedNodes());
    }


    public void ungroupNodes(ObjectSet<NodeWidget> nodes) {
        if(nodes == null || nodes.size == 0) return;

        for(NodeGroup other: groups) {
            other.removeWrappers(nodes);
        }

        TalosMain.Instance().ProjectController().setDirty();
    }

    public void removeGroup(NodeGroup nodeGroup) {
        groups.removeValue(nodeGroup, true);
        nodeGroup.remove();
    }

    private Array<NodeGroup> getSelectedGroups() {
        Array<NodeGroup> selectedGroups = new Array<>();
        ObjectSet<NodeWidget> nodes = getSelectedNodes();
        for(NodeGroup group: groups) {
            boolean isFullyContained = true;
            for(NodeWidget node: group.getNodes()) {
                if(!nodes.contains(node)) {
                    isFullyContained = false;
                    break;
                }
            }
            if(isFullyContained) {
                //add this group
                selectedGroups.add(group);
            }
        }

        return selectedGroups;
    }

    public void userSelectionApply (Rectangle rectangle) {
        clearSelection();
        Rectangle moduleRect = new Rectangle();
        for(int i = 0; i < nodes.size; i++) {
            NodeWidget node = nodes.get(i);
            tmp.set(node.getX(), node.getY());
            tmp.add(mainContainer.getX(), mainContainer.getY());
            localToStageCoordinates(tmp);
            moduleRect.set(tmp.x, tmp.y, node.getWidth(), node.getHeight());
            boolean hit = Intersector.intersectRectangles(rectangle, moduleRect, moduleRect);

            if(hit) {
                // hit
                addNodeToSelection(node);
            }
        }
    }

}
