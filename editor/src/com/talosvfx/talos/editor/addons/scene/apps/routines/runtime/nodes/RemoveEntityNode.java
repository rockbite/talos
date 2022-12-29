package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes;

import com.talosvfx.talos.editor.addons.scene.SceneUtils;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineNode;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;

public class RemoveEntityNode extends RoutineNode {

    @Override
    public void receiveSignal(String portName) {
        GameObject target = (GameObject) routineInstanceRef.getSignalPayload();
        target.parent.removeObject(target);
    }
}
