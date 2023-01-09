package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.AsyncRoutineNodeState;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineNode;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.TickableNode;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import lombok.Getter;

public class MoveTowardsNode extends RoutineNode implements TickableNode {

    @Getter
    protected Array<AsyncRoutineNodeState<GameObject>> states = new Array<>();

    private Array<GameObject> tmpArr = new Array<>();

    private boolean modifyingX;
    private boolean modifyingY;

    @Override
    public void receiveSignal(String portName) {
        modifyingX = isPortConnected("x");
        modifyingY = isPortConnected("y");

        GameObject signalPayload = (GameObject)routineInstanceRef.getSignalPayload();
        AsyncRoutineNodeState<GameObject> state =  Pools.obtain(AsyncRoutineNodeState.class);
        state.setTarget(signalPayload);
        state.alpha = 0;

        states.add(state);
    }

    public void tick(float delta) {
        if (states.isEmpty()) return;

        for(int i = states.size - 1; i >= 0; i--) {
            AsyncRoutineNodeState<GameObject> state = states.get(i);

            GameObject target = state.getTarget();
            TransformComponent component = target.getComponent(TransformComponent.class);

            float speed = fetchFloatValue("speed");

            boolean reachedX = false;
            boolean reachedY = false;

            if(modifyingX) {
                float curr = component.position.x;
                float end = fetchFloatValue("x");
                float direction = curr >= end ? -1 : 1;
                component.position.x += direction * speed * delta;
                if((curr < end && component.position.x >= end) || (curr > end && component.position.x <= end)) {
                    component.position.x = end;
                    reachedX = true;
                }
            }

            if(modifyingY) {
                float curr = component.position.y;
                float end = fetchFloatValue("y");
                float direction = curr >= end ? -1 : 1;
                component.position.y += direction * speed * delta;
                if((curr < end && component.position.y >= end) || (curr > end && component.position.y <= end)) {
                    component.position.y = end;
                    reachedY = true;
                }
            }

            if((modifyingX && modifyingY) == (reachedX && reachedY)) {
                if(reachedX || reachedY) {
                    freeState(i);
                }
            }
        }

        for(GameObject target: tmpArr) {
            // this now needs to send signal to next guy
            routineInstanceRef.setSignalPayload(target);
            sendSignal("onComplete");
        }
        tmpArr.clear();
    }

    private void freeState(int i) {
        AsyncRoutineNodeState<GameObject> state = states.get(i);
        GameObject target = state.getTarget();
        tmpArr.add(target);
        states.removeIndex(i);
        Pools.free(state);
    }

    @Override
    public void reset() {
        super.reset();
        for(int i = states.size - 1; i >= 0; i--) {
            AsyncRoutineNodeState<GameObject> state = states.get(i);
            Pools.free(state);
        }

        states.clear();
    }
}
