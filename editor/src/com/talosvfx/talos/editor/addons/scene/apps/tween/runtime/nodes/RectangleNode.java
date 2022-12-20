package com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.nodes;

import com.badlogic.gdx.math.Vector2;
import com.talosvfx.talos.editor.addons.scene.apps.tween.runtime.RoutineNode;

public class RectangleNode extends RoutineNode {

    @Override
    public Object queryValue(String targetPortName) {
        Vector2 pos = fetchVector2Value("center");
        Vector2 size = fetchVector2Value("size");

        if(targetPortName.equals("left")) {
            return  pos.x - size.x/2f;
        }
        if(targetPortName.equals("right")) {
            return  pos.x + size.x/2f;
        }
        if(targetPortName.equals("top")) {
            return  pos.y + size.y/2f;
        }
        if(targetPortName.equals("bottom")) {
            return  pos.y - size.y/2f;
        }
        return 0;
    }
}
