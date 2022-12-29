package com.talosvfx.talos.editor.addons.scene.apps.routines.nodes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.*;
import com.talosvfx.talos.editor.addons.scene.apps.routines.RoutineStage;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineInstance;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineNode;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes.AsyncRoutineNode;
import com.talosvfx.talos.editor.nodes.NodeBoard;
import com.talosvfx.talos.editor.nodes.NodeWidget;
import com.talosvfx.talos.editor.nodes.widgets.AbstractWidget;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;

public abstract class AbstractRoutineNodeWidget extends NodeWidget {

    protected ObjectMap<String, Object> params = new ObjectMap<>();

    private boolean animatingSignal = false;
    private boolean animatingInput = false;

    @Override
    public void init(Skin skin, NodeBoard nodeBoard) {
        super.init(skin, nodeBoard);

        headerTable.setBackground(ColorLibrary.obtainBackground(getSkin(), "node-header", ColorLibrary.BackgroundColor.GREEN));
    }

    @Override
    public void setSelected(boolean selected) {
        if (selected) {
            headerTable.setBackground(ColorLibrary.obtainBackground(getSkin(), "node-header", ColorLibrary.BackgroundColor.LIGHT_GREEN));
        } else {
            headerTable.setBackground(ColorLibrary.obtainBackground(getSkin(), "node-header", ColorLibrary.BackgroundColor.GREEN));
        }
    }

    @Override
    public void read (Json json, JsonValue jsonValue) {
        super.read(json, jsonValue);

        JsonValue properties = jsonValue.get("properties");
        Array<String> keys = widgetMap.keys().toArray();
        for (int i = 0; i < keys.size; i++) {
            String name = keys.get(i);
            JsonValue value = properties.get(name);

            if (value != null) {
                widgetMap.get(name).read(json, value);
            }
        }

        readProperties(properties);
    }

    @Override
    public void write (Json json) {
        super.write(json);

        json.writeObjectStart("properties");

        for(String name: widgetMap.keys()) {
            AbstractWidget widget = widgetMap.get(name);
            widget.write(json, name);
        }

        writeProperties(json);

        json.writeObjectEnd();
    }

    protected void readProperties(JsonValue properties) {

    }
    protected void writeProperties(Json json) {

    }

    public void animateSignal(String portName) {
        Array<Connection> connections = outputs.get(portName);

        if (connections == null) {
            return;
        }

        for(Connection connection : connections) {
            String targetSlot = connection.targetSlot;

            if (targetSlot == null) {
                return;
            }
            // animate the signal
            animateSignal(portName, connection);
        }
    }

    public void animateInput(String portName) {
        Array<Connection> connections = inputs.get(portName);

        if (connections == null) {
            return;
        }

        Connection connection = connections.first();

        animateInput(portName, connection);
    }

    public void animateInput(String fromSlot, Connection connection) {
        if(animatingInput) return;

        animatingInput = true;

        Color color = Color.valueOf("#0957a8");
        Actor tmpActor = new Actor();
        addActor(tmpActor);
        NodeBoard.NodeConnection nodeConnection = nodeBoard.findConnection(connection.targetNode, this, connection.targetSlot, fromSlot);
        nodeConnection.setHighlightActorBasic(tmpActor);
        tmpActor.setColor(NodeBoard.curveColor);

        tmpActor.addAction(Actions.sequence(
                Actions.color(color, 0.05f),
                Actions.color(NodeBoard.curveColor, 0.5f),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        nodeConnection.unsetHighlightActor();
                        tmpActor.remove();
                        animatingInput = false;
                    }
                })
        ));
    }

    public void animateSignal(String fromSlot, Connection connection) {
        if(animatingSignal) return;

        animatingSignal = true;

        Actor source = getOutputSlotActor(fromSlot);
        Actor target = connection.targetNode.getInputSlotActor(connection.targetSlot);

        Color original = new Color(source.getColor());
        Color color = Color.valueOf("#42f58a");

        source.clearActions();
        target.clearActions();

        source.addAction(Actions.sequence(
                        Actions.color(color, 0.1f),
                        Actions.color(original, 0.2f)
                ));

        Actor tmpActor = new Actor();
        addActor(tmpActor);
        NodeBoard.NodeConnection nodeConnection = nodeBoard.findConnection(this, connection.targetNode, fromSlot, connection.targetSlot);
        nodeConnection.setHighlightActor(tmpActor);
        tmpActor.setColor(color);

        tmpActor.addAction(Actions.sequence(
                Actions.moveTo(1, 0, 0.2f),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        nodeConnection.unsetHighlightActor();
                        tmpActor.remove();
                        animatingSignal = false;
                    }
                })
        ));


        target.addAction(Actions.sequence(
                Actions.delay(0.2f),
                Actions.color(color, 0.05f),
                Actions.color(original, 0.4f)
        ));
    }


    protected float getWidgetFloatValue(String name) {
        return getWidgetFloatValue(name, null);
    }

    protected float getWidgetFloatValue(String name, ObjectMap<String, Object> params) {
        Object widgetValue = getWidgetValue(name, params);
        if(widgetValue instanceof Integer) {
            return (Integer)widgetValue;
        }

        return (Float)widgetValue;
    }

    protected boolean getWidgetBooleanValue(String name) {
        Object widgetValue = getWidgetValue(name, null);
        boolean result = false;
        if(widgetValue instanceof Integer) {
            result = (Integer)widgetValue > 0;
        } else if(widgetValue instanceof Float) {
            result = (Float)widgetValue > 0;
        } else if(widgetValue instanceof Boolean) {
            result = (Boolean)widgetValue;
        }

        return result;
    }

    protected Object getWidgetValue(String name) {
        return getWidgetValue(name, null);
    }

    protected Object getWidgetValue(String name, ObjectMap<String, Object> params) {
        AbstractWidget widget = getWidget(name);
        Array<Connection> connections = getInputs().get(name);

        if(widget == null) return 0f;

        if(connections == null || connections.size == 0) {
            return widget.getValue();
        } else {
            Connection first = connections.first();

            animateInput(name, first);

            AbstractRoutineNodeWidget targetNode = (AbstractRoutineNodeWidget) first.targetNode;
            return targetNode.getOutputValue(first.targetSlot, params);
        }
    }

    public void reset() {

    }

    public Object getOutputValue(String name, ObjectMap<String, Object> params) {
        return 0f;
    }

    public float getDelta() {
        return ((RoutineStage)nodeBoard.getNodeStage()).getDelta();
    }

    public <T> T getNodeInstance() {
        RoutineStage nodeStage = (RoutineStage) nodeBoard.getNodeStage();
        RoutineInstance routineInstance = nodeStage.data.getRoutineInstance();
        int uniqueId = getUniqueId();
        T node = (T)routineInstance.getNodeById(uniqueId);

        return node;
    }


}

