package com.talosvfx.talos.runtime.routine.nodes;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.runtime.routine.RoutineNode;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.SavableContainer;

public class GOSelectorNode extends RoutineNode {

    private Array<GameObject> gameObjects = new Array<>();

    @Override
    public void receiveSignal(String portName) {
        SavableContainer container = routineInstanceRef.getContainer();

        if(container == null) return;

        gameObjects.clear();
        String target = fetchStringValue("target");
        if (target == null) {
            gameObjects = container.findGameObjects("");
        } else {
            gameObjects = container.findGameObjects(target);
        }

        routineInstanceRef.storeGlobal("executedTargets", gameObjects);

        int i = 0;
        routineInstanceRef.beginDepth();
        for(GameObject gameObject: gameObjects) {
            // make sure sendSignal supports payloads
            routineInstanceRef.setSignalPayload(gameObject);
            sendSignal("outSignal");
            routineInstanceRef.setDepthValue(i++);
        }
        routineInstanceRef.endDepth();
    }


    @Override
    public Object queryValue(String targetPortName) {
        SavableContainer container = routineInstanceRef.getContainer();

        if(container == null) return null;

        gameObjects.clear();
        String target = fetchStringValue("target");
        if (target == null) {
            gameObjects = container.findGameObjects("");
        } else {
            gameObjects = container.findGameObjects(target);
        }

        return gameObjects;
    }
}
