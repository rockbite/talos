/*******************************************************************************
 * Copyright 2019 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.talosvfx.talos.runtime;

import com.talosvfx.talos.runtime.values.NumericalValue;

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

    static public final Expression divide = new Expression() {
        @Override
        public void apply(NumericalValue a, NumericalValue b, NumericalValue out) {
            a.div(b, out);
        }
    };


    static public final Expression mod = new Expression() {
        @Override
        public void apply(NumericalValue a, NumericalValue b, NumericalValue out) {
            a.mod(b, out);
        }
    };
}
