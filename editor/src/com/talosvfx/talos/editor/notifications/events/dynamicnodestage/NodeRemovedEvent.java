package com.talosvfx.talos.editor.notifications.events.dynamicnodestage;

import com.talosvfx.talos.editor.nodes.DynamicNodeStage;
import com.talosvfx.talos.editor.nodes.NodeWidget;
import com.talosvfx.talos.editor.notifications.ContextRequiredEvent;
import com.talosvfx.talos.editor.notifications.TalosEvent;

public class NodeRemovedEvent extends ContextRequiredEvent<DynamicNodeStage<?>> {

    private NodeWidget node;

    @Override
    public void reset () {
        node = null;
    }

    public NodeRemovedEvent set (DynamicNodeStage<?> context, NodeWidget node) {
        setContext(context);
        this.node = node;
        return this;
    }

    public NodeWidget getNode() {
        return node;
    }
}
