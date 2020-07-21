package com.talosvfx.talos.editor.notifications.events;

import com.talosvfx.talos.editor.nodes.NodeWidget;
import com.talosvfx.talos.editor.notifications.Notifications;

public class NodeRemovedEvent implements Notifications.Event {

    private NodeWidget node;

    @Override
    public void reset () {
        node = null;
    }

    public NodeRemovedEvent set (NodeWidget node) {
        this.node = node;
        return this;
    }
}
