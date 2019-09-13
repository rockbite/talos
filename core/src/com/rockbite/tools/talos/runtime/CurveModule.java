package com.rockbite.tools.talos.runtime;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.rockbite.tools.talos.runtime.values.FloatValue;
import com.rockbite.tools.talos.runtime.values.Value;

import java.util.Comparator;


public class CurveModule extends Module {

    public static final int ALPHA = 0;

    FloatValue alphaVal = new FloatValue();
    FloatValue result = new FloatValue();

    private Array<Vector2> points = new Array();

    private Vector2 tmp = new Vector2();

    Comparator<Vector2> comparator = new Comparator<Vector2>() {
        @Override
        public int compare(Vector2 o1, Vector2 o2) {
            if(o1.x < o2.x) return -1;
            if(o1.x > o2.x) return 1;

            return 0;
        }
    };

    @Override
    public void init(ParticleSystem system) {
        super.init(system);

        createInputSlots(1);
        outputValues.put(0, result);

        resetPoints();
    }

    private void resetPoints() {
        // need to guarantee at least one point
        points.clear();
        Vector2 point = new Vector2(0, 0.5f);
        points.add(point);
    }

    public Array<Vector2> getPoints() {
        return points;
    }


    public int createPoint(float x, float y) {

        if(x < 0) x = 0;
        if(x > 1) x = 1;
        if(y < 0) y = 0;
        if(y > 1) y = 1;

        Vector2 point = new Vector2(x, y);
        points.add(point);

        sortPoints();

        return points.indexOf(point, true);
    }

    private void sortPoints() {
        points.sort(comparator);
    }

    @Override
    public void processValues(ScopePayload scopePayload) {
        getInputValue(alphaVal, ALPHA, scopePayload);
        float alpha = (float) alphaVal.get();

        alphaVal.set(interpolate(alpha));
        outputValues.put(0, alphaVal);

    }

    private float interpolate(float alpha) {
        // interpolate alpha in this point space

        if(points.get(0).x > 0) {
            return interpolate(alpha, 0, points.get(0).y, points.get(0).x, points.get(0).y);
        }

        for(int i = 0; i < points.size-1; i++) {
            Vector2 from = points.get(i);
            Vector2 to = points.get(i+1);
            if(alpha > from.x && alpha <= to.x) {
                return interpolate(alpha, from.x, from.y, to.x, to.y);
            }
        }

        if(points.get(points.size-1).x < 1f) {
            return interpolate(alpha, points.get(points.size-1).x, points.get(points.size-1).y, 1, points.get(points.size-1).y);
        }

        return 0;
    }

    private float interpolate(float alpha, float x1, float y1, float x2, float y2) {

        if(x1 == x2) return y1;

        tmp.set(x2, y2);
        tmp.sub(x1, y1);
        tmp.scl(alpha);
        tmp.add(x1, y1);

        return tmp.y;
    }

    public void removePoint(int i) {
        if(points.size > 1) {
            points.removeIndex(i);
        }
    }
}
