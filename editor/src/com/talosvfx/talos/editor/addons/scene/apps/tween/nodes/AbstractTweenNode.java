package com.talosvfx.talos.editor.addons.scene.apps.tween.nodes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.nodes.NodeBoard;
import com.talosvfx.talos.editor.nodes.NodeWidget;
import com.talosvfx.talos.editor.nodes.widgets.AbstractWidget;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;

public abstract class AbstractTweenNode extends NodeWidget {

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

        for(String name: widgetMap.keys()) {
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

    protected void sendSignal(String portName, String command, Object[] payload) {
        Array<Connection> connections = outputs.get(portName);

        if (connections == null)
            throw new GdxRuntimeException("Output port with name: '" + portName + "' + not found on this node, to send signal");

        for(Connection connection : connections) {
            String targetSlot = connection.targetSlot;

            if (targetSlot == null) throw new GdxRuntimeException("Output port is not connected to any input");

            AbstractTweenNode targetNode = (AbstractTweenNode) connection.targetNode; // this is a bold assumption, but I'll go with it :D

            // animate the signal
            animateSignal(portName, connection);

            targetNode.onSignalReceived(command, payload);
        }
    }

    private void animateSignal(String fromSlot, Connection connection) {
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
                    }
                })
        ));


        target.addAction(Actions.sequence(
                Actions.delay(0.2f),
                Actions.color(color, 0.05f),
                Actions.color(original, 0.4f)
        ));
    }

    protected abstract void onSignalReceived(String command, Object[] payload);
}
