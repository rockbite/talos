package com.rockbite.tools.talos.runtime;

import com.badlogic.gdx.math.MathUtils;

public abstract class Expression {

    abstract public float apply(float a, float b);

    static public final Expression sum = new Expression() {
        @Override
        public float apply(float a, float b) {
            return a + b;
        }
    };

    static public final Expression substract = new Expression() {
        @Override
        public float apply(float a, float b) {
            return a - b;
        }
    };

    static public final Expression multiply = new Expression() {
        @Override
        public float apply(float a, float b) {
            return a * b;
        }
    };

    static public final Expression devide = new Expression() {
        @Override
        public float apply(float a, float b) {
            if (b == 0) return 0; // we don't like to crash in this side of planet
            return a / b;
        }
    };

    static public final Expression cos = new Expression() {
        @Override
        public float apply(float a, float b) {
            return MathUtils.cosDeg(a);
        }
    };

    static public final Expression sin = new Expression() {
        @Override
        public float apply(float a, float b) {
            return MathUtils.sinDeg(a);
        }
    };
}
