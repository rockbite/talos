package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineNode;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.SavableContainer;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;

public class GetTransformNode extends RoutineNode {

    private Array<GameObject> gameObjects = new Array<>();

    @Override
    public Object queryValue(String targetPortName) {
        SavableContainer container = routineInstanceRef.getContainer();

        if(container == null) return 0;
        gameObjects.clear();
        String target = fetchStringValue("target");
        if (target == null) {
            gameObjects = container.findGameObjects("");
        } else {
            gameObjects = container.findGameObjects(target);
        }

        if(!gameObjects.isEmpty()) {
            TransformComponent component = gameObjects.first().getComponent(TransformComponent.class);
            if(targetPortName.equals("x")) {
                return component.position.x;
            } else if(targetPortName.equals("y")) {
                return component.position.y;
            }
        }

        return 0;
    }
}
