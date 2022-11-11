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

package com.talosvfx.talos.runtime.modules;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.values.NumericalValue;

import java.util.Comparator;

public class CurveModule extends AbstractModule {

    public static final int ALPHA = 0;
    public static final int OUTPUT = 0;

    NumericalValue alpha;
    NumericalValue output;

    private Array<Vector2> points;

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
    public void init () {
        super.init();
        resetPoints();
    }

    @Override
    protected void defineSlots() {
        alpha = createAlphaInputSlot(ALPHA);

        output = createOutputSlot(OUTPUT);
    }

    private void resetPoints() {
        // need to guarantee at least one point
        points = new Array<>();
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
    public void processCustomValues () {
        fetchInputSlotValue(ALPHA);
        output.set(interpolate(alpha.getFloat()));
    }

    private float interpolate(float alpha) {
        // interpolate alpha in this point space

        if(points.get(0).x >= 0 && alpha <= points.get(0).x) {
            return points.get(0).y;
        }

        for(int i = 0; i < points.size-1; i++) {
            Vector2 from = points.get(i);
            Vector2 to = points.get(i+1);
            if(alpha > from.x && alpha <= to.x) {
                float localAlpha = 1f;
                if(from.x != to.x) {
                    localAlpha = (alpha - from.x) / (to.x - from.x);
                }
                return interpolate(localAlpha, from.x, from.y, to.x, to.y);
            }
        }

        if(points.get(points.size-1).x <= 1f && alpha >= points.get(points.size-1).x) {
            return points.get(points.size-1).y;
        }

        return 0;
    }

    private float interpolate(float alpha, float x1, float y1, float x2, float y2) {
        if(y1 == y2) return y1;
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

    @Override
    public void write (Json json) {
        super.write(json);
        json.writeArrayStart("points");
        for (Vector2 point : getPoints()) {
            json.writeObjectStart();
            json.writeValue("x", point.x);
            json.writeValue("y", point.y);
            json.writeObjectEnd();
        }
        json.writeArrayEnd();
    }

    @Override
    public void read (Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        points.clear();
        final JsonValue points = jsonData.get("points");
        for (JsonValue point : points) {
            createPoint(point.get(0).asFloat(), point.get(1).asFloat());
        }
    }
}
