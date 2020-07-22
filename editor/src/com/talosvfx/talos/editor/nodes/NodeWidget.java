package com.talosvfx.talos.editor.nodes;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.widgets.ui.EditableLabel;

public abstract class NodeWidget extends EmptyWindow {

    EditableLabel title;

    protected IntMap<Image> inputSlotMap = new IntMap<>();
    protected IntMap<Image> outputSlotMap = new IntMap<>();

    NodeBoard nodeBoard;

    private int hoveredSlot = -1;
    private boolean hoveredSlotIsInput = false;
    private Vector2 tmp = new Vector2();
    private Vector2 tmp2 = new Vector2();
    private NodeWidget lastAttachedNode;

    protected IntArray inputSlots = new IntArray();
    protected IntArray outputSlots = new IntArray();

    protected ObjectMap<Integer, Connection> inputs = new ObjectMap();
    protected ObjectMap<Integer, Connection> outputs = new ObjectMap();
    protected Table dynamicContentTable;
    private int id = 0;

    public void graphUpdated () {

    }

    public void setId (int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public class Connection {
        public int targetSlot;
        public NodeWidget targetNode;

        public Connection(NodeWidget targetNode, int targetSlot) {
            this.targetNode = targetNode;
            this.targetSlot = targetSlot;
        }
    }

    public void init(Skin skin, NodeBoard nodeBoard) {
        super.init(skin);
        this.nodeBoard = nodeBoard;

        setBackground(skin.getDrawable("window"));

        title = new EditableLabel("Node Title", skin);
        add(title).expandX().top().left().padTop(8).padLeft(15).height(15);

        Table content = new Table();
        row();
        add(content).expand().grow().top().pad(7, 10, 17, 9);

        dynamicContentTable = new Table();

        content.add(dynamicContentTable).growX().expand().top();




        pack();
        layout();

        setModal(false);
        setMovable(true);

        setWidth(200);

        addCaptureListener(new InputListener() {

            Vector2 tmp = new Vector2();
            Vector2 prev = new Vector2();

            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                prev.set(x, y);
                NodeWidget.this.localToStageCoordinates(prev);
                nodeBoard.nodeClicked(NodeWidget.this);
                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                tmp.set(x, y);
                NodeWidget.this.localToStageCoordinates(tmp);
                super.touchDragged(event, x, y, pointer);
                nodeBoard.wrapperMovedBy(NodeWidget.this, tmp.x - prev.x, tmp.y - prev.y);

                prev.set(tmp);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                nodeBoard.nodeClickedUp(NodeWidget.this);
                event.cancel();
            }
        });
    }

    public void setConfig(XmlReader.Element config) {
        String titleString = config.getAttribute("name");
        title.setText(titleString);
    }

    @Override
    public float getTitlePrefWidth () {
        return title.getPrefWidth();
    }

    @Override
    public float getDragPadTop () {
        return 32;
    }

    /*
    protected void addConnection(String title, int key, int align) {
        if(align == Align.top || align == Align.bottom) {
            throw new GdxRuntimeException("node connections can be only from left or right");
        }

        Table slotRow = new Table();
        Image icon = new Image(getSkin().getDrawable("node-connector-off"));
        VisLabel label = new VisLabel(title, "small");

        if(Align.isLeft(align)) {
            slotRow.add(icon).left().padLeft(2);
            slotRow.add(label).left().padBottom(4).padLeft(5).padRight(10);
            leftTable.add(slotRow).left().expandX().top().padTop(5);
            leftTable.row();
            configureNodeActions(icon, key, true);

            inputSlots.add(key);
        }
        if(Align.isRight(align)) {
            slotRow.add(label).right().padBottom(4).padLeft(10).padRight(5);
            slotRow.add(icon).right().padRight(2);
            rightTable.add(slotRow).right().expandX().top().padTop(5);
            rightTable.row();
            configureNodeActions(icon, key, false);

            outputSlots.add(key);
        }
    }
     */

    private void configureNodeActions (Image icon, int key, boolean isInput) {
        if(isInput) {
            inputSlotMap.put(key, icon);
        } else {
            outputSlotMap.put(key, icon);
        }

        icon.addListener(new ClickListener() {

            private Vector2 tmp = new Vector2();
            private Vector2 tmp2 = new Vector2();

            private NodeWidget currentWrapper;

            private boolean currentIsInput = false;

            private int currentSlot;

            private boolean dragged;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                currentIsInput = isInput;
                currentWrapper = NodeWidget.this;
                tmp.set(x, y);
                icon.localToStageCoordinates(tmp);
                tmp2.set(icon.getWidth()/2f, icon.getHeight()/2f);
                icon.localToStageCoordinates(tmp2);

                currentSlot = key;

                dragged = false;

                NodeBoard.NodeConnection connection = nodeBoard.findConnection(NodeWidget.this, isInput, key);

                if(isInput && connection!= null) {
                    nodeBoard.removeConnection(connection);
                    nodeBoard.ccCurrentlyRemoving = true;

                    connection.fromNode.getOutputSlotPos(connection.fromId, tmp2);
                    currentIsInput = false;
                    currentWrapper = connection.fromNode;
                    currentSlot = connection.fromId;
                    nodeBoard.setActiveCurve(tmp2.x, tmp2.y, tmp.x, tmp.y, false);
                } else {
                    // we are creating new connection
                    nodeBoard.setActiveCurve(tmp2.x, tmp2.y, tmp.x, tmp.y, isInput);
                }

                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                super.touchDragged(event, x, y, pointer);
                tmp.set(x, y);
                icon.localToStageCoordinates(tmp);
                nodeBoard.updateActiveCurve(tmp.x, tmp.y);

                dragged = true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                nodeBoard.connectNodeIfCan(currentWrapper, currentSlot, currentIsInput);
                nodeBoard.ccCurrentlyRemoving = false;

                if(!dragged) {
                    // clicked
                    slotClicked(currentSlot, currentIsInput);
                }
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                hoveredSlot = key;
                hoveredSlotIsInput = isInput;
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                super.exit(event, x, y, pointer, toActor);
                hoveredSlot = -1;
            }
        });
    }

    public void getInputSlotPos(int id, Vector2 tmp) {
        if(inputSlotMap.get(id) == null) return;
        tmp.set(inputSlotMap.get(id).getWidth()/2f, inputSlotMap.get(id).getHeight()/2f);
        inputSlotMap.get(id).localToStageCoordinates(tmp);
    }

    public void getOutputSlotPos(int id, Vector2 tmp) {
        if(outputSlotMap.get(id) == null) return;
        tmp.set(outputSlotMap.get(id).getWidth()/2f, outputSlotMap.get(id).getHeight()/2f);
        outputSlotMap.get(id).localToStageCoordinates(tmp);
    }

    public void setSlotActive(int slotTo, boolean isInput) {
        if(isInput) {
            if(inputSlotMap.get(slotTo) == null) return;
            inputSlotMap.get(slotTo).setDrawable(getSkin().getDrawable("node-connector-on"));
        } else {
            if(outputSlotMap.get(slotTo) == null) return;
            outputSlotMap.get(slotTo).setDrawable(getSkin().getDrawable("node-connector-on"));
        }
    }

    public void setSlotInactive (int toId, boolean isInput) {
        if(isInput) {
            inputSlotMap.get(toId).setDrawable(getSkin().getDrawable("node-connector-off"));
            inputs.remove(toId);
        } else {
            outputSlotMap.get(toId).setDrawable(getSkin().getDrawable("node-connector-off"));
            outputs.remove(toId);
            lastAttachedNode = null;
        }
    }

    public boolean findHoveredSlot(int[] result) {
        if(hoveredSlot >= 0) {
            result[0] = hoveredSlot;
            if(hoveredSlotIsInput) {
                result[1] = 0;
            } else {
                result[1] = 1;
            }

            return true;
        }

        result[0] = -1;
        result[1] = -1;
        return false;
    }

    public void slotClicked(int slotId, boolean isInput) {
        //: todo do autojump
    }

    public void attachNodeToMyInput(NodeWidget node, int mySlot, int targetSlot) {
        inputs.put(mySlot, new Connection(node, targetSlot));
    }

    public void attachNodeToMyOutput(NodeWidget node, int mySlot, int targetSlot) {
        outputs.put(mySlot, new Connection(node, targetSlot));
    }

    public IntArray getInputSlots () {
        return inputSlots;
    }

    public ObjectMap<Integer, Connection> getInputs() {
        return inputs;
    }

    public IntArray getOutputSlots () {
        return outputSlots;
    }

    public void constructNode(XmlReader.Element module) {
        int rowCount = module.getChildCount();
        for (int i = 0; i < rowCount; i++) {
            XmlReader.Element row = module.getChild(i);

            // find class for row from map and instantiate widget using reflection

            // pass row element to that widget to configure it

            // add that widget to dynamicContentTable

            // ask widget if it contains port, and declare that port

        }
    }
}
