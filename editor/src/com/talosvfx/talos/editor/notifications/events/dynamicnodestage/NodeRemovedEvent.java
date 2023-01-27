package com.talosvfx.talos.editor.notifications.events.dynamicnodestage;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.nodes.DynamicNodeStage;
import com.talosvfx.talos.editor.nodes.NodeWidget;
import com.talosvfx.talos.editor.notifications.ContextRequiredEvent;
import com.talosvfx.talos.editor.notifications.TalosEvent;
import lombok.Getter;

public class NodeRemovedEvent extends ContextRequiredEvent<DynamicNodeStage<?>> {

    @Getter
    private Array<NodeWidget> nodes = new Array<>();

    @Override
    public void reset () {
        nodes.clear();
    }

}
