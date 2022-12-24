package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes;

import com.badlogic.gdx.math.Vector2;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineNode;

public class RectangleNode extends RoutineNode {

    Vector2 pos = new Vector2();
    Vector2 size = new Vector2();

    @Override
    public Object queryValue(String targetPortName) {
        Vector2 posGet = fetchVector2Value("center");
        Vector2 sizeGet = fetchVector2Value("size");

        if(posGet != null) {
            pos.set(posGet);
        }
        if(sizeGet != null) {
            size.set(sizeGet);
        }

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
