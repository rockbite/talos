package com.talosvfx.talos.editor.nodes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.*;
import com.kotcrab.vis.ui.FocusManager;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.WorkplaceStage;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.events.NodeCreatedEvent;
import com.talosvfx.talos.editor.utils.GridRenderer;

public abstract class DynamicNodeStage extends WorkplaceStage implements Json.Serializable {

    protected XmlReader.Element nodeData;
    public Skin skin;
    protected NodeBoard nodeBoard;
    private Image selectionRect;

    private NodeListPopup nodeListPopup;

    public DynamicNodeStage (Skin skin) {
        super();
        this.skin = skin;
        nodeData = loadData();
    }

    protected abstract XmlReader.Element loadData();

    @Override
    public void init () {
        bgColor.set(0.15f, 0.15f, 0.15f, 1f);

        nodeListPopup = new NodeListPopup(nodeData);
        nodeListPopup.setListener(new NodeListPopup.NodeListListener() {
            @Override
            public void chosen (Class clazz, XmlReader.Element module, float x, float y) {
                if(NodeWidget.class.isAssignableFrom(clazz)) {
                    NodeWidget node = createNode(clazz, x, y);
                    if(node != null) {
                        node.constructNode(module);
                        Notifications.fireEvent(Notifications.obtainEvent(NodeCreatedEvent.class).set(node));
                    }
                }
            }
        });

        initActors();
        initListeners();
    }

    public NodeListPopup getNodeListPopup() {
        return nodeListPopup;
    }

    public XmlReader.Element getConfigForNodeClass(Class clazz) {
        return nodeListPopup.getConfigFor(clazz);
    }

    public void showPopup() {
        final Vector2 vec = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        stage.screenToStageCoordinates(vec);

        nodeListPopup.showPopup(stage, vec);
    }
    public NodeWidget createNode (Class<? extends NodeWidget> clazz, float x, float y) {
        return nodeBoard.createNode(clazz, nodeListPopup.getConfigFor(clazz), x, y);
    }

    protected void initActors() {
        GridRenderer gridRenderer = new GridRenderer(stage);
        stage.addActor(gridRenderer);

        nodeBoard = new NodeBoard(skin, this);

        stage.addActor(nodeBoard);

        selectionRect = new Image(skin.getDrawable("orange_row"));
        selectionRect.setSize(0, 0);
        selectionRect.setVisible(false);
        stage.addActor(selectionRect);
    }

    @Override
    protected void initListeners () {
        super.initListeners();

        stage.addListener(new InputListener() {

            boolean dragged = false;

            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                dragged = false;
                return true;
            }

            @Override
            public void touchDragged (InputEvent event, float x, float y, int pointer) {
                super.touchDragged(event, x, y, pointer);
                dragged = true;
            }

            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {

                if(button == 0 && !event.isCancelled()) {
                    FocusManager.resetFocus(getStage());
                    nodeBoard.clearSelection();
                }

                if(button == 1 && !event.isCancelled()) {
                    showPopup();
                }
            }

            @Override
            public boolean keyDown(InputEvent event, int keycode) {

                if(keycode == Input.Keys.ENTER) {
                    stage.getCamera().position.set(0, 0, 0);
                    ((OrthographicCamera)stage.getCamera()).zoom = 1.0f;
                }

                if(keycode == Input.Keys.G && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    //moduleBoardWidget.createGroupFromSelectedWrappers();
                }

                if(keycode == Input.Keys.U && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    //moduleBoardWidget.ungroupSelectedWrappers();
                }

                if(keycode == Input.Keys.C && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    nodeBoard.copySelectedModules();
                }

                if(keycode == Input.Keys.V && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    nodeBoard.pasteFromClipboard();
                }

                if(keycode == Input.Keys.A && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    nodeBoard.selectAllNodes();
                }

                if(keycode == Input.Keys.Z && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && !Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                    TalosMain.Instance().ProjectController().undo();
                }

                if(keycode == Input.Keys.Z && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                    TalosMain.Instance().ProjectController().redo();
                }

                return super.keyDown(event, keycode);
            }
        });
    }

    public void write (Json json) {
        Array<NodeWidget> nodes = nodeBoard.nodes;

        json.writeArrayStart("list");
        for (NodeWidget node: nodes) {
            json.writeValue(node);
        }
        json.writeArrayEnd();

        json.writeArrayStart("connections");
        for (NodeBoard.NodeConnection connection: nodeBoard.nodeConnections) {
            json.writeObjectStart();
            json.writeValue("fromNode", connection.fromNode.getUniqueId());
            json.writeValue("toNode", connection.toNode.getUniqueId());
            json.writeValue("fromSlot", connection.fromId);
            json.writeValue("toSlot", connection.toId);
            json.writeObjectEnd();
        }
        json.writeArrayEnd();
    }

    public void read (Json json, JsonValue root) {
        reset();

        JsonValue nodes = root.get("list");
        JsonValue connections = root.get("connections");

        int idCounter = 0;

        IntMap<NodeWidget> nodeMap = new IntMap<>();

        for (JsonValue nodeData: nodes) {
            String className = nodeData.getString("name");

            Class clazz = nodeListPopup.getNodeClassByName(className);
            if(clazz != null) {
                NodeWidget node = createNode(clazz, 0, 0);
                node.constructNode(nodeListPopup.getModuleByClassName(className));
                node.read(json, nodeData);
                idCounter = Math.max(idCounter, node.getUniqueId());
                nodeMap.put(node.getUniqueId(), node);
            }
        }

        nodeBoard.globalNodeCounter = idCounter;

        for (JsonValue connectionData: connections) {
            int fromNode = connectionData.getInt("fromNode");
            int toNode = connectionData.getInt("toNode");
            String fromSlot = connectionData.getString("fromSlot");
            String toSlot = connectionData.getString("toSlot");

            NodeWidget fromWidget = nodeMap.get(fromNode);
            NodeWidget toWidget = nodeMap.get(toNode);

            nodeBoard.makeConnection(fromWidget, toWidget, fromSlot, toSlot);
        }

    }

    public void reset () {
        nodeBoard.reset();
    }
}
