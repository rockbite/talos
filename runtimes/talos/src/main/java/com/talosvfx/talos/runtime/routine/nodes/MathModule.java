package com.talosvfx.talos.runtime.routine.nodes;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.talosvfx.talos.runtime.routine.RoutineNode;

public class MathModule extends RoutineNode {

    Vector2 tmp = new Vector2();

    @Override
    public Object queryValue(String targetPortName) {

        String operation = fetchStringValue("operation");

        Object valueA = fetchValue("valueA");
        Object valueB = fetchValue("valueB");

        // todo: this all needs a rethink to by type safe and fast
        if(valueA instanceof String && valueB instanceof Float) {
            valueA = Float.parseFloat((String) valueA);
        }
        if(valueB instanceof String && valueA instanceof Float) {
            valueB = Float.parseFloat((String) valueB);
        }
        if(valueA instanceof String && valueB instanceof Integer) {
            if (((String) valueA).contains(".")) valueA = (int)Float.parseFloat((String) valueA);
            else valueA = Integer.parseInt((String) valueA);
        }
        if(valueB instanceof String && valueA instanceof Integer) {
            if (((String) valueB).contains(".")) valueB = (int)Float.parseFloat((String) valueB);
            else valueB = Integer.parseInt((String) valueB);
        }

        if(valueA instanceof Integer && valueB instanceof Float) {
            valueA = (float)((int)valueA);
        }
        if(valueB instanceof Integer && valueA instanceof Float) {
            valueB = (float)((int)valueB);
        }

        if(valueA instanceof Float && valueB instanceof Float) {
            return performOperation(operation, (float)valueA, (float)valueB);
        } else if(valueA instanceof Integer && valueB instanceof Integer) {
            return performOperation(operation, (int)valueA, (int)valueB);
        }else if(valueA instanceof Vector2 && valueB instanceof Vector2) {
            return performOperation(operation, (Vector2)valueA, (Vector2)valueB);
        }

        return super.queryValue(targetPortName);
    }

    private float performOperation(String operation, float a, float b) {
        if(operation.equals("ADD")) {
            return a + b;
        } else if (operation.equals("SUB")) {
            return a - b;
        } else if (operation.equals("MUL")) {
            return a * b;
        } else if (operation.equals("DIV")) {
            if(b == 0) return  0;
            return a / b;
        } else if (operation.equals("POW")) {
            return (float) Math.pow(a, b);
        } else if (operation.equals("SIN")) {
            return MathUtils.sinDeg(a) * b;
        } else if (operation.equals("COS")) {
            return MathUtils.cosDeg(a) * b;
        }

        return 0;
    }

    private Vector2 performOperation(String operation, Vector2 a, Vector2 b) {
        tmp.set(performOperation(operation, a.x, b.x), performOperation(operation, a.y, b.y));

        return tmp;
    }
}
