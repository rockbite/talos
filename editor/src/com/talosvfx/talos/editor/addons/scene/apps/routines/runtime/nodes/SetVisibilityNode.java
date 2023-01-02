package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes;

import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineNode;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.SpriteRendererComponent;

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
