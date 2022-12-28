package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes;

import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineNode;

public abstract class AsyncRoutineNode<T extends AsyncRoutineNode.AsyncRoutineNodeState> extends RoutineNode {

    public static class AsyncRoutineNodeState {
        float alpha;
    }

    // override fetching of variables, so that before fetching it sets "fetch payload",
    // so when fetching it uses that payload to provide proper data, which for example will be used by stagger node or other
    // this is similar to for depth info
    // in this case the payload here will be: GO, and it's index (idl if index should be somehow mixed with depth shit)

    @Override
    public void receiveSignal(String portName) {

        // when signal is received, get the payload and make sure it's a GameObject
        // create a state object for it and store it here in a map for <GO, State>
        // reset the state

    }

    public void tick() {

        // for each state process it's alpha
        // make sure to user interpolations
        // make sure to perform yoyo logic

        // delegate to some other tick method of whoever extends this so they can set some vars based on this alpha
        // make sure to provide the GO in question

        // when it's finished call the end signal

    }
}
