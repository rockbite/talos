package com.talosvfx.talos.editor.notifications.events;

import com.talosvfx.talos.editor.nodes.NodeBoard;
import com.talosvfx.talos.editor.notifications.TalosEvent;

public class NodeConnectionRemovedEvent implements TalosEvent {

    private NodeBoard.NodeConnection connection;

    @Override
    public void reset () {
        connection = null;
    }

    public NodeConnectionRemovedEvent set(NodeBoard.NodeConnection connection) {
        this.connection = connection;

        return this;
    }

    public NodeBoard.NodeConnection getConnection() {
        return connection;
    }
}
