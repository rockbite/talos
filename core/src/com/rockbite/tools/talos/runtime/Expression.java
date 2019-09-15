package com.rockbite.tools.talos.runtime;

import com.badlogic.gdx.math.MathUtils;
import com.rockbite.tools.talos.runtime.values.NumericalValue;

public abstract class Expression {

    abstract public void apply(NumericalValue a, NumericalValue b, NumericalValue out);


    static public final Expression sum = new Expression() {
        @Override
        public void apply (NumericalValue a, NumericalValue b, NumericalValue out) {
            a.sum(b, out);
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
            return MathUtils.cosDeg(a) * b;
        }
    };

    static public final Expression sin = new Expression() {
        @Override
        public float apply(float a, float b) {
            return MathUtils.sinDeg(a) * b;
        }
    };
}
