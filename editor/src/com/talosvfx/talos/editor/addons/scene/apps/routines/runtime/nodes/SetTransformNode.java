package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineNode;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.ISizableComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.RoutineRendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;

public class SetTransformNode extends RoutineNode {

    Vector2 diff = new Vector2();
    Array<GameObject> list = new Array<>();

    @Override
    public void receiveSignal(String portName) {

        GameObject go = (GameObject) routineInstanceRef.getSignalPayload();
        if(go != null) {
            TransformComponent component = go.getComponent(TransformComponent.class);

            diff.set(component.position);

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
            diff.set(component.position.x - diff.x, component.position.y - diff.y);

            if(diff.len() > 0) {
                //apply to sub routines
                applySubRoutineTransforms(go);
            }

            sendSignal("onComplete");
        }
    }

    private void applySubRoutineTransforms(GameObject go) {
        list.clear();
        Array<GameObject> result = go.findGOsWithComponents(list, RoutineRendererComponent.class);

        for (GameObject gameObject : result) {
            RoutineRendererComponent component = gameObject.getComponent(RoutineRendererComponent.class);
            component.routineInstance.applyQuadDiff(diff);
        }

    }
}
