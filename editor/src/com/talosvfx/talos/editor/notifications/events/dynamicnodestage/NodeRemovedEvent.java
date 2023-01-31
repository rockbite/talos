package com.talosvfx.talos.editor.notifications.events.dynamicnodestage;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.nodes.DynamicNodeStage;
import com.talosvfx.talos.editor.nodes.NodeWidget;
import com.talosvfx.talos.editor.notifications.events.AbstractContextRequiredEvent;
import lombok.Getter;

public class NodeRemovedEvent extends AbstractContextRequiredEvent<DynamicNodeStage<?>> {

    @Getter
    private Array<NodeWidget> nodes = new Array<>();

    @Override
    public void reset () {
        nodes.clear();
    }

}
