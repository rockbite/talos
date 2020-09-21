package com.talosvfx.talos.editor.addons.treedata.nodes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.XmlWriter;
import com.talosvfx.talos.editor.nodes.NodeWidget;
import com.talosvfx.talos.editor.nodes.PluginNodeWidget;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.plugins.TalosPluginProvider;

import java.io.IOException;

public class BasicDataNode<T extends TalosPluginProvider> extends PluginNodeWidget<T> implements Notifications.Observer {

    public BasicDataNode (Skin skin) {
        super(skin);
    }

    public void writeXML(XmlWriter xmlWriter) throws IOException {

        String outputTagName = outputSlots.first();

        XmlWriter element = xmlWriter.element(outputTagName);

        for (String slotName: inputSlots) {


            if(inputs.get(slotName) != null) {
                // we have a connection let's fetch the data
                NodeWidget targetNode = inputs.get(slotName).targetNode;

                if (targetNode instanceof BasicDataNode) {
                    BasicDataNode basicTargetNode = (BasicDataNode) targetNode;
                    basicTargetNode.writeXML(element);
                }
            } else {
                XmlWriter field = element.element(slotName);
                // lets just get local data
                String value = getValueFromObject(widgetMap.get(slotName).getValue());
                if(value == null) {
                    // lets get default
                    value = defaultsMap.get(slotName);
                    if(value == null) value = "";
                }
                field.text(value);
                field.pop();
            }


        }

        element.pop();
    }

    public String getValueFromObject(Object valueObject) {
        if (valueObject instanceof Float) {
            return valueObject.toString();
        } else if (valueObject instanceof Color) {
            return valueObject.toString();
        } else {
            return "";
        }
    }
}
