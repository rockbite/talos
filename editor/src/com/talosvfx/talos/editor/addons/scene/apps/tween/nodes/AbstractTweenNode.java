package com.talosvfx.talos.editor.addons.scene.apps.tween.nodes;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
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
        Connection connection = outputs.get(portName);

        if(connection == null) throw new GdxRuntimeException("Output port with name: '" + portName + "' + not found on this node, to send signal");

        String targetSlot = connection.targetSlot;

        if(targetSlot == null) throw new GdxRuntimeException("Output port is not connected to any input");

        AbstractTweenNode targetNode = (AbstractTweenNode) connection.targetNode; // this is a bold assumption, but I'll go with it :D

        // animate the signal
        animateSignal(connection);

        targetNode.onSignalReceived(command, payload);
    }

    private void animateSignal(Connection connection) {

    }

    protected abstract void onSignalReceived(String command, Object[] payload);
}
