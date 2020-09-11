package com.talosvfx.talos.editor.nodes;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.talosvfx.talos.editor.addons.shader.nodes.Vector2Node;
import com.talosvfx.talos.editor.nodes.widgets.*;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.events.NodeDataModifiedEvent;
import com.talosvfx.talos.editor.widgets.ui.EditableLabel;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;

public abstract class NodeWidget extends ResizableWindow implements Json.Serializable {

    EditableLabel title;

    protected ObjectMap<String, Table> inputSlotMap = new ObjectMap<>();
    protected ObjectMap<String, Table> outputSlotMap = new ObjectMap<>();

    NodeBoard nodeBoard;

    private String hoveredSlot = null;
    private boolean hoveredSlotIsInput = false;
    private Vector2 tmp = new Vector2();
    private Vector2 tmp2 = new Vector2();
    private NodeWidget lastAttachedNode;

    protected Array<String> inputSlots = new Array();
    protected Array<String> outputSlots = new Array();

    protected ObjectMap<String, AbstractWidget> widgetMap = new ObjectMap();

    protected ObjectMap<String, String> typeMap = new ObjectMap();
    protected ObjectMap<String, String> defaultsMap = new ObjectMap();

    protected ObjectMap<String, Connection> inputs = new ObjectMap();
    protected ObjectMap<String, Connection> outputs = new ObjectMap();
    private int id = 0;
    private int uniqueId = 0;

    private final ObjectMap<String, Class<? extends AbstractWidget>> widgetClassMap = new ObjectMap<>();

    private Table widgetContainer = new Table();
    private Table headerTable;
    private String nodeName;

    public NodeWidget (Skin skin) {
        super(skin);
    }

    public void graphUpdated () {

    }

    public void setId (int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setUniqueId (int uniqueId) {
        this.uniqueId = uniqueId;
    }

    public int getUniqueId() {
        return uniqueId;
    }

    public void setSelected(boolean selected) {
        if (selected) {
            headerTable.setBackground(ColorLibrary.obtainBackground(getSkin(), "node-header", ColorLibrary.BackgroundColor.LIGHT_BLUE));
        } else {
            headerTable.setBackground(ColorLibrary.obtainBackground(getSkin(), "node-header", ColorLibrary.BackgroundColor.RED));
        }
    }

    public class Connection {
        public String targetSlot;
        public NodeWidget targetNode;

        public Connection(NodeWidget targetNode, String targetSlot) {
            this.targetNode = targetNode;
            this.targetSlot = targetSlot;
        }
    }

    private void initMaps() {
        widgetClassMap.put("value", LabelWidget.class);
        widgetClassMap.put("select", SelectWidget.class);
        widgetClassMap.put("checkbox", CheckBoxWidget.class);
        widgetClassMap.put("color", ColorWidget.class);
        widgetClassMap.put("dynamicValue", ValueWidget.class);
        // group is handled manually for now
    }

    public void init(Skin skin, NodeBoard nodeBoard) {
        initMaps();
        this.nodeBoard = nodeBoard;

        Stack mainStack = new Stack();
        Table backgroundTable = new Table();
        Table contentTable = new Table();

        contentTable.top();

        mainStack.add(backgroundTable);
        mainStack.add(contentTable);

        headerTable = new Table();
        Table bodyTable = new Table();

        headerTable.setBackground(ColorLibrary.obtainBackground(getSkin(), "node-header", ColorLibrary.BackgroundColor.RED));
        bodyTable.setBackground(ColorLibrary.obtainBackground(getSkin(), "node-body", ColorLibrary.BackgroundColor.WHITE));
        setBackground(ColorLibrary.obtainBackground(getSkin(), "node-shadow-border", ColorLibrary.BackgroundColor.WHITE));

        backgroundTable.add(headerTable).growX().height(32).row();
        backgroundTable.add(bodyTable).grow().row();

        title = new EditableLabel("Node Title", skin);
        headerTable.add(title).expandX().top().left().padLeft(12).height(15);

        contentTable.add(widgetContainer).padLeft(16).padRight(16).growX().top().padTop(32);
        contentTable.row();

        addAdditionalContent(contentTable);

        contentTable.add().height(15).row();

        add(mainStack).minWidth(266).grow().pad(15);
        pack();

        setModal(false);
        setMovable(true);
        setResizable(true);

        addListener(new ClickListener() {

            Vector2 tmp = new Vector2();
            Vector2 prev = new Vector2();

            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                prev.set(x, y);
                NodeWidget.this.localToStageCoordinates(prev);
                if(nodeBoard != null) {
                    nodeBoard.nodeClicked(NodeWidget.this);
                }
                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                tmp.set(x, y);
                NodeWidget.this.localToStageCoordinates(tmp);
                super.touchDragged(event, x, y, pointer);
                if(nodeBoard != null) {
                    nodeBoard.wrapperMovedBy(NodeWidget.this, tmp.x - prev.x, tmp.y - prev.y);
                }

                prev.set(tmp);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                if(nodeBoard != null) {
                    nodeBoard.nodeClickedUp(NodeWidget.this);
                }
//                event.cancel();
            }
        });
    }

    protected void addAdditionalContent(Table contentTable) {

    }

    @Override
    public void invalidateHierarchy() {
        super.invalidateHierarchy();
    }

    public void setConfig(XmlReader.Element config) {
        nodeName = config.getAttribute("name");

        String titleString = config.getAttribute("title");
        title.setText(titleString);
    }

    protected void addConnection(AbstractWidget widget, String variableName, boolean isInput) {
        Table portTable = widget.addPort(isInput);

        if (isInput) {
             configureNodeActions(portTable, variableName, true);
            inputSlots.add(variableName);
        } else {
            configureNodeActions(portTable, variableName, false);
            outputSlots.add(variableName);
        }
    }


    private void configureNodeActions (Table port, String key, boolean isInput) {
        if(isInput) {
            inputSlotMap.put(key, port);
        } else {
            outputSlotMap.put(key, port);
        }

        port.addListener(new ClickListener() {

            private Vector2 tmp = new Vector2();
            private Vector2 tmp2 = new Vector2();

            private NodeWidget currentWrapper;

            private boolean currentIsInput = false;

            private String currentSlot;

            private boolean dragged;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                currentIsInput = isInput;
                currentWrapper = NodeWidget.this;
                tmp.set(x, y);
                port.localToStageCoordinates(tmp);
                tmp2.set(port.getWidth()/2f, port.getHeight()/2f);
                port.localToStageCoordinates(tmp2);

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
                port.localToStageCoordinates(tmp);
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
                hoveredSlot = null;
            }
        });
    }

    public void getInputSlotPos(String id, Vector2 tmp) {
        if(inputSlotMap.get(id) == null) return;
        tmp.set(inputSlotMap.get(id).getWidth()/2f, inputSlotMap.get(id).getHeight()/2f);
        inputSlotMap.get(id).localToStageCoordinates(tmp);
    }

    public void getOutputSlotPos(String id, Vector2 tmp) {
        if(outputSlotMap.get(id) == null) return;
        tmp.set(outputSlotMap.get(id).getWidth()/2f, outputSlotMap.get(id).getHeight()/2f);
        outputSlotMap.get(id).localToStageCoordinates(tmp);
    }

    public void setSlotActive(String slotTo, boolean isInput) {
        if(isInput) {
            if(inputSlotMap.get(slotTo) == null) return;
            //inputSlotMap.get(slotTo).setDrawable(getSkin().getDrawable("node-connector-on"));
        } else {
            if(outputSlotMap.get(slotTo) == null) return;
            //outputSlotMap.get(slotTo).setDrawable(getSkin().getDrawable("node-connector-on"));
        }
    }

    public void setSlotInactive (String toId, boolean isInput) {
        if(isInput) {
            //inputSlotMap.get(toId).setDrawable(getSkin().getDrawable("node-connector-off"));
            inputs.remove(toId);
        } else {
            //outputSlotMap.get(toId).setDrawable(getSkin().getDrawable("node-connector-off"));
            outputs.remove(toId);
            lastAttachedNode = null;
        }
    }

    public boolean findHoveredSlot(Object[] result) {
        if(hoveredSlot != null) {
            result[0] = hoveredSlot;
            if(hoveredSlotIsInput) {
                result[1] = 0;
            } else {
                result[1] = 1;
            }

            return true;
        }

        result[0] = null;
        result[1] = -1;
        return false;
    }

    public void slotClicked(String slotId, boolean isInput) {
        //: todo do autojump
    }

    public void attachNodeToMyInput(NodeWidget node, String mySlot, String targetSlot) {
        inputs.put(mySlot, new Connection(node, targetSlot));
    }

    public void attachNodeToMyOutput(NodeWidget node, String mySlot, String targetSlot) {
        outputs.put(mySlot, new Connection(node, targetSlot));
    }

    public Array<String> getInputSlots () {
        return inputSlots;
    }

    public ObjectMap<String, Connection> getInputs() {
        return inputs;
    }

    public Array<String> getOutputSlots () {
        return outputSlots;
    }

    private void addRow(XmlReader.Element row, int index, int count, boolean isGroup) {
        String tagName = row.getName();
        Class<? extends AbstractWidget> clazz = widgetClassMap.get(tagName);

        if (clazz != null) {
            try {
                AbstractWidget widget = ClassReflection.newInstance(clazz);
                widget.init(getSkin());

                widget.loadFromXML(row);

                if(widget instanceof ValueWidget && isGroup) {
                    ValueWidget valueWidget = (ValueWidget) widget;
                    if(index == 0) {
                        valueWidget.setType(ValueWidget.Type.TOP);
                    } else if(index == count - 1) {
                        valueWidget.setType(ValueWidget.Type.BOTTOM);
                    } else {
                        valueWidget.setType(ValueWidget.Type.MID);
                    }
                }

                if (isGroup) {
                    float padTop = 0;
                    if(index == 0) {
                        padTop = 10;
                    }
                    widgetContainer.add(widget).padTop(padTop).padBottom(1).growX().row();
                } else {
                    widgetContainer.add(widget).padTop(10).padBottom(10).growX().row();
                }

                String variableName = row.getAttribute("name");

                widgetMap.put(variableName, widget);
                typeMap.put(variableName, row.getAttribute("type", "float"));
                defaultsMap.put(variableName, row.getAttribute("default", "0.0"));

                // does it have a port?
                if(row.hasAttribute("port")) {
                    String portType = row.getAttribute("port", "input");
                    if(portType.equals("input")) {
                        addConnection(widget, variableName, true);
                    } else if (portType.equals("output")) {
                        addConnection(widget, variableName, false);
                    }
                }

                // init listener
                widget.addListener(new ChangeListener() {
                    @Override
                    public void changed (ChangeEvent changeEvent, Actor actor) {
                        Notifications.fireEvent(Notifications.obtainEvent(NodeDataModifiedEvent.class).set(NodeWidget.this));
                    }
                });

            } catch (ReflectionException exception) {

            }
        } else {
            if (tagName.equals("group")) {
                XmlReader.Element group = row;
                for (int i = 0; i < group.getChildCount(); i++) {
                    XmlReader.Element groupRow = group.getChild(i);
                    if(groupRow.getName().equals("dynamicValue") || groupRow.getName().equals("value")) {
                        addRow(groupRow, i, group.getChildCount(), true);
                    }
                }
            }
        }
    }

    public void constructNode(XmlReader.Element module) {
        int rowCount = module.getChildCount();
        for (int i = 0; i < rowCount; i++) {
            XmlReader.Element row = module.getChild(i);
            addRow(row, i, rowCount, false);
        }

        widgetContainer.add().growY().row();
        pack();
    }

    @Override
    public void write(Json json) {
        json.writeValue("name", getNodeName());
        json.writeValue("id", getUniqueId());
        json.writeValue("title", title.getText());
        json.writeObjectStart("position");
        json.writeValue("x", getX() + "");
        json.writeValue("y", getY() + "");
        json.writeObjectEnd();
    }

    private String getNodeName () {
        return nodeName;
    }

    @Override
    public void read(Json json, JsonValue jsonValue) {
        if(title == null) return;

        title.setText(jsonValue.getString("title", "Empty"));
        setUniqueId(jsonValue.getInt("id"));
        JsonValue position = jsonValue.get("position");
        if(position != null) {
            setX(position.getFloat("x", 0));
            setY(position.getFloat("y", 0));
        } else {
            setX(0);
            setY(0);
        }
    }
}
