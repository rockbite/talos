package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineNode;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.SavableContainer;
import com.talosvfx.talos.editor.addons.scene.logic.Scene;

public class RoutineExecutorNode extends RoutineNode {

    private String title;

    private Array<GameObject> gameObjects = new Array<>();

    @Override
    public void receiveSignal(String portName) {
        SavableContainer container = routineInstanceRef.getContainer();

        if(container == null) return;

        gameObjects.clear();
        Object signalPayload = routineInstanceRef.getSignalPayload();
        if(signalPayload != null && signalPayload instanceof GameObject) {
            gameObjects.add((GameObject) signalPayload);
        }

        if(gameObjects.isEmpty()) {
            String target = fetchStringValue("target");
            if (target == null) {
                gameObjects = container.findGameObjects("");
            } else {
                gameObjects = container.findGameObjects(target);
            }
        }

        routineInstanceRef.storeGlobal("executedTargets", gameObjects);

        for(GameObject gameObject: gameObjects) {
            // make sure sendSignal supports payloads
            routineInstanceRef.setSignalPayload(gameObject);
            sendSignal("outSignal");
        }
    }

    @Override
    protected void configureNode(JsonValue properties) {
        super.configureNode(properties);
        Object val = inputs.get("title").valueOverride;
        String title = val != null ? (String) val : "";

        routineInstanceRef.getCustomLookup().put(title, this);

        configured = true;
    }

    public String getTitle() {
        return propertiesJson.getString("title", "");
    }
}
