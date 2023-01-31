package com.talosvfx.talos.editor.notifications.events.dynamicnodestage;

import com.talosvfx.talos.editor.nodes.DynamicNodeStage;
import com.talosvfx.talos.editor.nodes.NodeWidget;
import com.talosvfx.talos.editor.notifications.ContextRequiredEvent;
import com.talosvfx.talos.editor.notifications.events.AbstractContextRequiredEvent;

public class NodeCreatedEvent extends AbstractContextRequiredEvent<DynamicNodeStage<?>> {

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
