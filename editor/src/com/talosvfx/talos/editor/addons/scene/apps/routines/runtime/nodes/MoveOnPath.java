package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes;

import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.AsyncRoutineNodeState;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.SavableContainer;
import com.talosvfx.talos.editor.addons.scene.logic.components.CurveComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import lombok.Getter;

public class MoveOnPath extends AsyncRoutineNode<GameObject, MoveOnPath.State> {

    private Array<GameObject> gameObjects = new Array<>();

    private Bezier<Vector2> bezier = new Bezier<>();
    private Vector2 tmp = new Vector2();

    public static class State extends AsyncRoutineNodeState<GameObject> {
        public GameObject curveGo;
        public Array<Float> lengthData = new Array<>();
        public float sumLength;


        @Override
        public void reset() {
            super.reset();
            curveGo = null;
            lengthData.clear();
        }
    }

    @Override
    protected MoveOnPath.State obtainState() {
        return Pools.obtain(MoveOnPath.State.class);
    }

    @Override
    protected boolean targetAdded(State state) {
        gameObjects.clear();

        SavableContainer container = routineInstanceRef.getContainer();

        if(container == null) return false;

        String target = fetchStringValue("target");
        if (target == null) {
            gameObjects = container.findGameObjects("");
        } else {
            gameObjects = container.findGameObjects(target);
        }

        if(gameObjects.size > 0) {
            GameObject go = gameObjects.first();
            if(go.hasComponent(CurveComponent.class)) {
                state.curveGo = go;
                return true;
            }
        }

        return false;
    }

    @Override
    protected void stateTick(State state, float delta) {
        createEvenlySpacedPoints(state);

        CurveComponent curve = state.curveGo.getComponent(CurveComponent.class);

        float currLen = 0;

        for(int i = 0; i < curve.getNumSegments(); i++) {
            Vector2[] pointsInSegment = curve.getPointsInSegment(i);
            bezier.set(pointsInSegment);
            float length = state.lengthData.get(i);
            float prevLen = currLen;
            currLen += length;
            float currA = currLen/state.sumLength;
            float prevA = prevLen/state.sumLength;
            if(state.alpha < currA) {
                float localAlpha = (state.alpha-prevA)/(currA-prevA);
                Vector2 point = bezier.valueAt(tmp, localAlpha);
                TransformComponent transform = state.getTarget().getComponent(TransformComponent.class);
                transform.position.set(point);
                break;
            }
        }
    }

    private void createEvenlySpacedPoints(State state) {
        if(state.lengthData.isEmpty()) {
            CurveComponent curve = state.curveGo.getComponent(CurveComponent.class);

            float sum = 0;
            for(int i = 0; i < curve.getNumSegments(); i++) {
                Vector2[] pointsInSegment = curve.getPointsInSegment(i);
                bezier.set(pointsInSegment);
                float l = bezier.approxLength(10);
                state.lengthData.add(l);
                sum+=l;
            }

            state.sumLength = sum;
        }
    }

}
