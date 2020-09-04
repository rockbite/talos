package com.talosvfx.talos.editor.notifications.events;

import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;
import com.talosvfx.talos.editor.addons.shader.nodes.ColorNode;
import com.talosvfx.talos.editor.nodes.NodeWidget;
import com.talosvfx.talos.editor.notifications.Notifications;

public class NodeDataModifiedEvent implements Notifications.Event {
    private NodeWidget node;

    @Override
    public void reset () {

    }

    public NodeDataModifiedEvent set (NodeWidget node) {
        this.node = node;
        return this;
    }

    public NodeWidget getNode() {
        return node;
    }
}
