package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineNode;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.SavableContainer;

public class RoutineExecutorNode extends RoutineNode {

    private SavableContainer container;

    @Override
    public void receiveSignal(String portName) {
        if(container == null) return;

        String target = fetchStringValue("target");

        Array<GameObject> gameObjects = container.findGameObjects(target);

        for(GameObject gameObject: gameObjects) {
            // make sure sendSignal supports payloads
            routineInstanceRef.setSignalPayload(gameObject);
            sendSignal("outSignal");
        }
    }

    public void setContainer(SavableContainer container) {
        this.container = container;
    }
}
