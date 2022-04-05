package com.talosvfx.talos.editor.notifications.events;

import com.talosvfx.talos.editor.nodes.NodeWidget;
import com.talosvfx.talos.editor.notifications.TalosEvent;

public class NodeDataModifiedEvent implements TalosEvent {
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
