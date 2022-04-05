package com.talosvfx.talos.editor.notifications.events;

import com.talosvfx.talos.editor.nodes.NodeWidget;
import com.talosvfx.talos.editor.notifications.TalosEvent;

public class NodeCreatedEvent implements TalosEvent {

    NodeWidget node;

    public NodeCreatedEvent set(NodeWidget node) {
        this.node = node;

        return this;
    }

    @Override
    public void reset () {
        node = null;
    }
}
