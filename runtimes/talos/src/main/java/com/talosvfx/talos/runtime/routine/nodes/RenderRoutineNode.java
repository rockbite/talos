package com.talosvfx.talos.runtime.routine.nodes;

import com.badlogic.gdx.math.Vector2;
import com.talosvfx.talos.runtime.routine.RoutineNode;

public class RenderRoutineNode extends RoutineNode {

    public Vector2 position = new Vector2();

    public Vector2 size = new Vector2();
    public Vector2 viewportPosition = new Vector2();
    public Vector2 viewportSize = new Vector2();


    @Override
    public void receiveSignal(String portName) {

        sendSignal("renderSignal");
    }

    @Override
    public Object queryValue(String targetPortName) {
        if(targetPortName.equals("position")) {
            return position;
        }
        if(targetPortName.equals("size")) {
            return size;
        }
        if(targetPortName.equals("viewportSize")) {
            return viewportSize;
        }
        if(targetPortName.equals("viewportPos")) {
            return viewportPosition;
        }

        return 0;
    }
}
