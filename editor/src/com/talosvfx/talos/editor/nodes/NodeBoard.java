package com.talosvfx.talos.editor.nodes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.Curve;
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

    private ObjectIntMap<Class<? extends NodeWidget>> nodeCounter = new ObjectIntMap<>();

    private ObjectSet<NodeWidget> selectedNodes = new ObjectSet<>();

    private DynamicNodeStage nodeStage;

    private NodeWidget ccFromNode = null;
    private int ccFromSlot = 0;
    private boolean ccCurrentIsInput = false;
    public boolean ccCurrentlyRemoving = false;
    private NodeWidget wasNodeSelectedOnDown = null;
    private NodeWidget wasNodeDragged = null;

    public Array<NodeConnection> nodeConnections = new Array<>();
    public Array<NodeWidget> nodes = new Array<>();

    public static class NodeConnection {
        public NodeWidget fromNode;
        public NodeWidget toNode;
        public int fromId;
        public int toId;
    }

    public NodeBoard(Skin skin, DynamicNodeStage nodeStage) {
        this.skin = skin;
        shapeRenderer = new ShapeRenderer();

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
            node = ClassReflection.newInstance(clazz);
            node.init(skin, this);
            node.setConfig(config);
            node.setPosition(x - node.getWidth()/2f, y - node.getHeight()/2f);
            addActor(node);
            nodes.add(node);

            int counter = nodeCounter.getAndIncrement(clazz, 0, 1);
            node.setId(counter);

            tryAndConnectLasCC(node);
        } catch (ReflectionException e) {
            e.printStackTrace();
        }

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

        removeActor(node);

        TalosMain.Instance().ProjectController().setDirty();
    }

    private <T extends AbstractModule> void tryAndConnectLasCC(NodeWidget nodeWidget) {
        if(ccFromNode != null) {
            Class fromClass;
            Slot fromSlotObject;
            IntArray toSlots;
            NodeWidget fromModule;
            NodeWidget toModule;
            int fromSlot = 0;
            int toSlot = 0;
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
                int slot = toSlots.get(i);
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


    public NodeConnection findConnection(NodeWidget node, boolean isInput, int key) {
        NodeConnection nodeToFind =  null;
        for(NodeConnection nodeConnection: nodeConnections) {
            if((isInput && nodeConnection.toId == key && node == nodeConnection.toNode) ||
                    (!isInput && nodeConnection.toId == key && node == nodeConnection.fromNode)) {
                // found the node let's remove it
                nodeToFind = nodeConnection;
            }
        }

        return nodeToFind;
    }

    public void removeConnection (NodeConnection connection) {
        Notifications.fireEvent(Notifications.obtainEvent(NodeConnectionRemovedEvent.class).set(connection));

        nodeConnections.removeValue(connection, true);

        connection.fromNode.setSlotInactive(connection.fromId, false);
        connection.toNode.setSlotInactive(connection.toId, true);

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

    public NodeConnection addConnectionCurve(NodeWidget from, NodeWidget to, int slotForm, int slotTo) {
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

    public void connectNodeIfCan(NodeWidget currentNode, int currentSlot, boolean currentIsInput) {
        int[] result = new int[2];
        NodeWidget target = null;
        boolean targetIsInput = false;
        // iterate over all widgets that are not current and see if mouse is over any of their slots, need to only connect input to output or output to input
        for(NodeWidget node: getNodes()) {
            if(node != currentNode) {
                node.findHoveredSlot(result);

                if(result[0] >= 0 ) {
                    // found match
                    target = node;
                    if(result[1] == 0) {
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
            int fromSlot, toSlot;

            if(targetIsInput) {
                fromWrapper = currentNode;
                toWrapper = target;
                fromSlot = currentSlot;
                toSlot = result[0];
            } else {
                fromWrapper = target;
                toWrapper = currentNode;
                fromSlot = result[0];
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

    public void makeConnection(NodeWidget from, NodeWidget to, int slotFrom, int slotTo) {
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
                wrapper.setBackground("window-blue");
            } else {
                wrapper.setBackground("window");
            }
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
            affectedNode.graphUpdated();
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
}