package com.talosvfx.talos.editor.notifications.events.dynamicnodestage;

import com.talosvfx.talos.editor.nodes.DynamicNodeStage;
import com.talosvfx.talos.editor.nodes.NodeBoard;
import com.talosvfx.talos.editor.notifications.ContextRequiredEvent;
import com.talosvfx.talos.editor.notifications.TalosEvent;
import com.talosvfx.talos.editor.notifications.events.AbstractContextRequiredEvent;

public class NodeConnectionRemovedEvent extends AbstractContextRequiredEvent<DynamicNodeStage<?>> {

        private NodeBoard.NodeConnection connection;

    @Override
    public void reset () {
        connection = null;
    }

    public NodeConnectionRemovedEvent set (DynamicNodeStage<?> nodeStage, NodeBoard.NodeConnection connection) {
        setContext(nodeStage);
        this.connection = connection;

        return this;
    }

    public NodeBoard.NodeConnection getConnection() {
        return connection;
    }
}
