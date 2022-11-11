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

package com.talosvfx.talos.runtime.utils;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * Java translation of this: https://thebookofshaders.com/edit.php#11/2d-snoise-clear.frag
 */
public class SimplexNoise {

    Vector2 v = new Vector2();
    Vector2 i = new Vector2();
    Vector2 i1 = new Vector2();
    Vector2 x0 = new Vector2();
    Vector2 x1 = new Vector2();
    Vector2 x2 = new Vector2();
    Vector3 p = new Vector3();
    Vector3 m = new Vector3();
    Vector3 X = new Vector3();
    Vector3 h = new Vector3();
    Vector3 ox = new Vector3();
    Vector3 a0 = new Vector3();
    Vector3 g = new Vector3();

    float[] C = new float[4];

    public SimplexNoise() {
        C[0] =  0.211324865405187f;
        C[1] =  0.366025403784439f;
        C[2] = -0.577350269189626f;
        C[3] =  0.024390243902439f;
    }

    public float query(float x, float y, float frequency) {
        v.set(x, y).scl(frequency);

        // First corner (x0)
        float dot = v.dot(C[1], C[1]);
        i.set((float)Math.floor(v.x + dot), (float)Math.floor(v.y + dot));
        dot = i.dot(C[0], C[0]);
        x0.set(v.x - i.x + dot, v.y - i.y + dot);

        // Other two corners (x1, x2)
        i1.set(0, 0);
        if(x0.x > x0.y) i1.set(1.0f, 0.0f); else i1.set(0.0f, 1.0f);
        x1.set(x0.x + C[0] - i1.x, x0.y + C[0] - i1.y);
        x2.set(x0.x + C[2], x0.y + C[2]);

        // Do some permutations to avoid
        // truncation effects in permutation
        i = mod289(i);

        p.set(i.y, i1.y + i.y, 1.0f + i.y);
        permute(p);
        p.add(i.x, i.x + i1.x, i.x + 1.0f);
        permute(p);

        m.set(0.5f - x0.dot(x0), 0.5f - x1.dot(x1), 0.5f - x2.dot(x2));
        m.x = Math.max(m.x, 0.0f);
        m.y = Math.max(m.y, 0.0f);
        m.z = Math.max(m.z, 0.0f);

        m.set(m.x * m.x, m.y * m.y, m.z * m.z);
        m.set(m.x * m.x, m.y * m.y, m.z * m.z);

        // Gradients:
        //  41 pts uniformly over a line, mapped onto a diamond
        //  The ring size 17*17 = 289 is close to a multiple
        //      of 41 (41*7 = 287)
        X.set(p.x * C[3], p.y * C[3], p.z * C[3]);
        fract(X);
        X.scl(2.0f).sub(1.0f);
        h.set(Math.abs(X.x) - 0.5f, Math.abs(X.y) - 0.5f, Math.abs(X.z) - 0.5f);
        ox.set((float)Math.floor(X.x + 0.5f), (float)Math.floor(X.y + 0.5f), (float)Math.floor(X.z + 0.5f));
        a0.set(X.x - ox.x, X.y - ox.y, X.z - ox.z);

        // Normalise gradients implicitly by scaling m
        // Approximation of: m *= inverse sqrt(a0*a0 + h*h);
        m.scl(1.79284291400159f - 0.85373472095314f * (a0.x*a0.x+h.x*h.x), 1.79284291400159f - 0.85373472095314f * (a0.y*a0.y+h.y*h.y), 1.79284291400159f - 0.85373472095314f * (a0.z*a0.z+h.z*h.z));

        // Compute final noise value at P
        g.set(0, 0, 0);
        g.x  = a0.x  * x0.x  + h.x  * x0.y;

        g.y = x1.x * a0.y + x1.y * h.y;
        g.z = x2.x * a0.z + x2.y * h.z;

        return 130.0f * m.dot(g);

    }

    private float fract(float v) {
        return (float) (v - Math.floor(v));
    }

    private Vector3 fract(Vector3 v) {
        v.x = fract(v.x);
        v.y = fract(v.y);
        v.z = fract(v.z);

        return v;
    }

    private float mod289(float v) {
        return (float) (v - Math.floor(v * (1.0f / 289.0f)) * 289.0f);
    }

    Vector2 mod289(Vector2 v) {
        v.x = mod289(v.x);
        v.y = mod289(v.y);
        return v;
    }

    private float permute(float x) {
        return (float) (((x*34.0)+1.0)*x);
    }

    private Vector3 permute(Vector3 x) {
        x.x = mod289(permute(x.x));
        x.y = mod289(permute(x.y));
        x.z = mod289(permute(x.z));

        return x;
    }
}
