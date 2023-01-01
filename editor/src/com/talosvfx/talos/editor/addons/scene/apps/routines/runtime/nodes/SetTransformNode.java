package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes;

import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineNode;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.ISizableComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;

public class SetTransformNode extends RoutineNode {

    @Override
    public void receiveSignal(String portName) {

        GameObject go = (GameObject) routineInstanceRef.getSignalPayload();
        if(go != null) {
            TransformComponent component = go.getComponent(TransformComponent.class);

            component.position.x = fetchFloatValue("x");
            component.position.y = fetchFloatValue("y");
            component.scale.x = fetchFloatValue("scaleX");
            component.scale.y = fetchFloatValue("scaleY");
            component.rotation = fetchFloatValue("rotation");
            component.pivot.x = fetchFloatValue("pivotX");
            component.pivot.y = fetchFloatValue("pivotY");

            ISizableComponent sizeComponent = go.findComponent(ISizableComponent.class);
            if(sizeComponent != null) {
                sizeComponent.setWidth(fetchFloatValue("width"));
                sizeComponent.setHeight(fetchFloatValue("height"));
            }

            sendSignal("onComplete");
        }
    }
}
