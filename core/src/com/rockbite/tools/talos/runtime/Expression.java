package com.rockbite.tools.talos.runtime;

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
        public void apply(NumericalValue a, NumericalValue b, NumericalValue out) {
            a.sub(b, out);
        }
    };

    static public final Expression multiply = new Expression() {
        @Override
        public void apply(NumericalValue a, NumericalValue b, NumericalValue out) {
            a.mul(b, out);
        }
    };

    static public final Expression cos = new Expression() {
        @Override
        public void apply(NumericalValue a, NumericalValue b, NumericalValue out) {
            a.cos(out);
            out.mul(b, out);
        }
    };

    static public final Expression sin = new Expression() {
        @Override
        public void apply(NumericalValue a, NumericalValue b, NumericalValue out) {
            a.sin(out);
            out.mul(b, out);
        }
    };

    static public final Expression pow = new Expression() {
        @Override
        public void apply(NumericalValue a, NumericalValue b, NumericalValue out) {
            a.pow(b, out);
        }
    };

    static public final Expression abs = new Expression() {
        @Override
        public void apply(NumericalValue a, NumericalValue b, NumericalValue out) {
            a.abs(out);
        }
    };
}
