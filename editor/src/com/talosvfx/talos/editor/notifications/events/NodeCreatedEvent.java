package com.talosvfx.talos.editor.notifications.events;

import com.talosvfx.talos.editor.nodes.NodeWidget;
import com.talosvfx.talos.editor.notifications.Notifications;

public class NodeCreatedEvent implements Notifications.Event {

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
