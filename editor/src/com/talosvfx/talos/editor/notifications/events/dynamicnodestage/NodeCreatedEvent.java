package com.talosvfx.talos.editor.notifications.events.dynamicnodestage;

import com.talosvfx.talos.editor.nodes.DynamicNodeStage;
import com.talosvfx.talos.editor.nodes.NodeWidget;
import com.talosvfx.talos.editor.notifications.ContextRequiredEvent;

public class NodeCreatedEvent extends ContextRequiredEvent<DynamicNodeStage<?>>  {

    NodeWidget node;

    public NodeCreatedEvent set (DynamicNodeStage<?> context, NodeWidget node) {
        setContext(context);
        this.node = node;

        return this;
    }

    @Override
    public void reset () {
        node = null;
    }
}
