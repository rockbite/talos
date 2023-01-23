package com.talosvfx.talos.runtime.routine.nodes;

import com.talosvfx.talos.runtime.routine.RoutineNode;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.components.SpriteRendererComponent;

public class SetVisibilityNode extends RoutineNode {

    @Override
    public void receiveSignal(String portName) {
        GameObject go = (GameObject) routineInstanceRef.getSignalPayload();

        if(go != null) {
            boolean res = fetchBooleanValue("isVisible");
            go.setEditorVisible(res);
            if(go.hasComponent(SpriteRendererComponent.class)) {
                SpriteRendererComponent component = go.getComponent(SpriteRendererComponent.class);
                component.visible = res;
                component.childrenVisible = res;
            }
        }

        sendSignal("outSignal");
    }
}
