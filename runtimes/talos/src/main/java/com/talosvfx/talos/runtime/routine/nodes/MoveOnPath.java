package com.talosvfx.talos.runtime.routine.nodes;

import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.talosvfx.talos.runtime.routine.AsyncRoutineNodeState;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.SavableContainer;
import com.talosvfx.talos.runtime.scene.components.CurveComponent;
import com.talosvfx.talos.runtime.scene.components.TransformComponent;

public class MoveOnPath extends AsyncRoutineNode<GameObject, MoveOnPath.State> {

    private Array<GameObject> gameObjects = new Array<>();

    private Bezier<Vector2> bezier = new Bezier<>();
    private Vector2 tmp = new Vector2();
    public transient Vector2[] tmpArr;
    private boolean reverse = false;

    private final Vector2 offset = new Vector2();
    private boolean useWorldOffset = true;

    public MoveOnPath() {
        tmpArr = new Vector2[]{new Vector2(), new Vector2(), new Vector2(), new Vector2()};
    }

    public static class State extends AsyncRoutineNodeState<GameObject> {

        public Array<Vector2> points = new Array<>();
        public Array<Float> lengthData = new Array<>();
        public float sumLength;


        @Override
        public void reset() {
            super.reset();
            points.clear();
            lengthData.clear();
        }
    }

    @Override
    protected State obtainState() {
        return Pools.obtain(State.class);
    }

    @Override
    protected boolean targetAdded(State state) {
        gameObjects.clear();

        reverse = fetchBooleanValue("reverse");
        useWorldOffset = fetchBooleanValue("useWorldOffset");

        SavableContainer container = routineInstanceRef.getContainer();

        if(container == null) return false;

        String target = fetchStringValue("target");
        if (target == null) {
            gameObjects = container.findGameObjects("");
        } else {
            gameObjects = container.findGameObjects(target);
        }

        if (gameObjects.size > 0) {
            GameObject go = gameObjects.first();
            // make sure offset is always set back to zero
            // so world transforms won't accumulate
            // or old state would not persist
            offset.setZero();
            if (go.hasComponent(TransformComponent.class)) {
                // store world offset
                TransformComponent.localToWorld(go, offset);
            }

            if (go.hasComponent(CurveComponent.class)) {
                copyPoints(go.getComponent(CurveComponent.class), state.points);

                float jitter = fetchFloatValue("jitter");
                if (jitter > 0) {
                    randomizeCurve(state.points, jitter);
                }

                return true;
            }
        }

        return false;
    }

    private void randomizeCurve(Array<Vector2> points, float jitter) {

        tmp.set(MathUtils.random(-1f, 1f) * jitter, MathUtils.random(-1f, 1f) * jitter);
        points.get(0).add(tmp);
        points.get(1).add(tmp);

        for (int i = 3; i < points.size - 2; i+=2) {
            tmp.set(MathUtils.random(-1f, 1f) * jitter, MathUtils.random(-1f, 1f) * jitter);
            points.get(i).add(tmp);
            points.get(i-1).add(tmp);
            points.get(i+1).add(tmp);
        }

        tmp.set(MathUtils.random(-1f, 1f) * jitter, MathUtils.random(-1f, 1f) * jitter);
        points.get(points.size - 1).add(tmp);
        points.get(points.size - 2).add(tmp);
    }

    private void copyPoints(CurveComponent component, Array<Vector2> points) {
        points.clear();
        for (Vector2 point : component.points) {
            points.add(new Vector2(point)); // todo: this can be pooled and then cleared on state reset
        }
    }

    @Override
    protected void stateTick(State state, float delta) {
        createEvenlySpacedPoints(state);

        float alpha = state.alpha;
        if (reverse) {
            alpha = 1f - alpha;
        }

        float currLen = 0;

        for (int i = 0; i < getNumSegments(state.points); i++) {
            Vector2[] pointsInSegment = getPointsInSegment(i, state.points);
            bezier.set(pointsInSegment);
            float length = state.lengthData.get(i);
            float prevLen = currLen;
            currLen += length;
            float currA = currLen/state.sumLength;
            float prevA = prevLen/state.sumLength;

            if(alpha <= currA) {
                float localAlpha = (alpha-prevA)/(currA-prevA);
                Vector2 point = bezier.valueAt(tmp, localAlpha);
                TransformComponent transform = state.getTarget().getComponent(TransformComponent.class);
                transform.position.set(point);
                if (useWorldOffset) {
                    // offset with transform of curve's gizmo
                    transform.position.add(offset);
                }
                break;
            }
        }
    }

    private void createEvenlySpacedPoints(State state) {
        if(state.lengthData.isEmpty()) {
            Array<Vector2> points = state.points;

            float sum = 0;
            for(int i = 0; i < getNumSegments(points); i++) {
                Vector2[] pointsInSegment = getPointsInSegment(i, points);
                bezier.set(pointsInSegment);
                float l = bezier.approxLength(30);
                state.lengthData.add(l);
                sum+=l;
            }

            state.sumLength = sum;
        }
    }

    private int getNumSegments( Array<Vector2> points) {
        return points.size / 3;
    }

    private Vector2[] getPointsInSegment(int index, Array<Vector2> points) {
        for(int i = 0; i < 4; i++) {
            if(i == 3) {
                tmpArr[i].set(points.get(loopIndex(index * 3 + i, points)));
            } else {
                tmpArr[i].set(points.get(index * 3 + i));
            }
        }
        return tmpArr;
    }

    private int loopIndex(int index, Array<Vector2> points) {
        return (index + points.size) % points.size;
    }

}
