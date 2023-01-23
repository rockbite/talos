package com.talosvfx.talos.runtime.routine.nodes;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.runtime.routine.AsyncRoutineNodeState;
import com.talosvfx.talos.runtime.routine.RoutineNode;
import com.talosvfx.talos.runtime.routine.TickableNode;
import com.talosvfx.talos.runtime.routine.misc.InterpolationLibrary;
import lombok.Getter;

public abstract class AsyncRoutineNode<U, T extends AsyncRoutineNodeState<U>> extends RoutineNode implements TickableNode {

    @Getter
    protected Array<T> states = new Array<>();

    private Array<U> tmpArr = new Array<>();

    private boolean isYoyo = false;
    private Interpolation interpolation = Interpolation.linear;
    private Object targets;

    // override fetching of variables, so that before fetching it sets "fetch payload",
    // so when fetching it uses that payload to provide proper data, which for example will be used by stagger node or other
    // this is similar to for depth info
    // in this case the payload here will be: GO, and it's index (idl if index should be somehow mixed with depth shit)

    protected T obtainState() {
        AsyncRoutineNodeState<U> state = Pools.obtain(AsyncRoutineNodeState.class);

        return (T) state;
    }

    @Override
    protected void constructNode(XmlReader.Element config) {
        super.constructNode(config);

        // hack in the interpolation
        Port port = new Port();
        port.name = "interpolation";
        port.nodeRef = this;
        port.connectionType = ConnectionType.DATA;
        port.portType = PortType.INPUT;
        inputs.put(port.name, port);
    }

    @Override
    public void receiveSignal(String portName) {
        U signalPayload = (U)routineInstanceRef.getSignalPayload();

        if(!supportsConcurrent()) {
            for (int i = states.size - 1; i >= 0; i--) {
                if (states.get(i).getTarget() == signalPayload) {
                    //states.get(i).alpha = 0;
                    return;
                }
            }
        }

        T state = obtainState();
        state.setTarget(signalPayload);
        state.alpha = 0;

        float duration = fetchFloatValue("duration");
        state.setDuration(duration);
        state.direction = 1;
        isYoyo = fetchBooleanValue("yoyo");
        String interpolationName = fetchStringValue("interpolation");
        interpolation = InterpolationLibrary.get(interpolationName);

        boolean success = targetAdded(state);

        if(success) {
            states.add(state);
        } else {
            Pools.free(state);
        }

        targets = routineInstanceRef.fetchGlobal("executedTargets");
    }

    protected boolean supportsConcurrent() {
        return false;
    }


    protected boolean targetAdded(T state) {
        return false;
    }

    public void tick(float delta) {
        if(states.isEmpty()) return;

        // for each state process it's alpha
        // make sure to use interpolations
        // make sure to perform yoyo logic

        // delegate to some other tick method of whoever extends this so they can set some vars based on this alpha
        // make sure to provide state in question

        // when it's finished call the end signal

        delta = processDelta(delta);

        tmpArr.clear();

        for(int i = states.size - 1; i >= 0; i--) {
            T state = states.get(i);
            state.alpha += state.direction * delta/state.getDuration();
            if(state.alpha > 1) state.alpha = 1;
            if(state.alpha < 0) state.alpha = 0;
            state.interpolatedAlpha = interpolation.apply(state.alpha);

            stateTick(state, delta);

            if(state.alpha >= 1 && state.direction == 1) {
                if(!isYoyo) {
                    freeState(i);
                } else {
                    state.direction *= -1;
                }
            }

            if(state.alpha <= 0 && state.direction == -1) {
                freeState(i);
            }
        }

        for(U target: tmpArr) {
            // this now needs to send signal to next guy
            routineInstanceRef.setSignalPayload(target);
            routineInstanceRef.storeGlobal("executedTargets", targets);
            sendSignal("onComplete");
        }
        tmpArr.clear();
    }

    protected float processDelta(float delta) {
        return delta;
    }

    private void freeState(int i) {
        T state = states.get(i);
        U target = state.getTarget();
        tmpArr.add(target);

        states.removeIndex(i);
        Pools.free(state);
    }

    protected abstract void stateTick(T state, float delta);

    @Override
    public void reset() {
        super.reset();
        for(int i = states.size - 1; i >= 0; i--) {
            T state = states.get(i);
            Pools.free(state);
        }

        states.clear();
    }
}
