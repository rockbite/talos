package com.talosvfx.talos.editor.notifications.events.dynamicnodestage;

import com.talosvfx.talos.editor.nodes.DynamicNodeStage;
import com.talosvfx.talos.editor.nodes.NodeWidget;
import com.talosvfx.talos.editor.notifications.ContextRequiredEvent;

public class NodeDataModifiedEvent extends ContextRequiredEvent<DynamicNodeStage<?>> {
    private NodeWidget node;
    public boolean isFastChange = false;

    @Override
    public void reset() {
        node = null;
        isFastChange = false;
    }

    public NodeDataModifiedEvent set (DynamicNodeStage<?> context, NodeWidget node) {
        setContext(context);
        this.node = node;
        return this;
    }

    public NodeWidget getNode() {
        return node;
    }
}
