package com.talosvfx.talos.editor.notifications.events.dynamicnodestage;

import com.talosvfx.talos.editor.nodes.DynamicNodeStage;
import com.talosvfx.talos.editor.nodes.NodeBoard;
import com.talosvfx.talos.editor.notifications.ContextRequiredEvent;

public class NodeConnectionCreatedEvent extends ContextRequiredEvent<DynamicNodeStage<?>> {

    private NodeBoard.NodeConnection connection;

    @Override
    public void reset () {
        connection = null;
    }

    public NodeConnectionCreatedEvent set (DynamicNodeStage<?> nodestage, NodeBoard.NodeConnection connection) {
        setContext(nodestage);
        this.connection = connection;

        return this;
    }

    public NodeBoard.NodeConnection getConnection() {
        return connection;
    }
}
