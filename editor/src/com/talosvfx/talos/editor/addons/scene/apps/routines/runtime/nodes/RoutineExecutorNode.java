package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes;

import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineNode;

public class RoutineExecutorNode extends RoutineNode {


    @Override
    public void receiveSignal(String portName) {
        String target = fetchStringValue("target");

        // make sure there is a way to inject GameObjectContainer to this node as the parent to search from.
        // maybe it should be part of the whole instance, and it can be injected on which container it's running

        // get reference to GameObjectContainer
        // find all matching GameObject targets in the game object container using a method from RoutineNode
        // make sure sendSignal supports payloads
        // call sendSignal with payload of each target (so call it multiple times)

        sendSignal("outSignal");

    }
}
