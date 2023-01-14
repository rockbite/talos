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

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.ScopePayload;
import com.talosvfx.talos.runtime.values.NumericalValue;

import java.util.Comparator;
import java.util.Random;

public class OffsetModule extends AbstractModule {

    public static final int ALPHA = 0;

    public static final int OUTPUT = 0;

    NumericalValue alpha;
    NumericalValue output;

    NumericalValue lowPos;
    NumericalValue lowSize;
    NumericalValue highPos;
    NumericalValue highSize;

    private int lowShape;
    private int highShape;

    private boolean lowEdge = true;
    private boolean highEdge = true;

    private float tolerance = 0;

    public static final int TYPE_SQUARE = 0;
    public static final int TYPE_ELLIPSE = 1;
    public static final int TYPE_LINE = 2;

    public static final int SIDE_ALL = 0;
    public static final int SIDE_TOP = 1;
    public static final int SIDE_BOTTOM = 2;
    public static final int SIDE_LEFT = 3;
    public static final int SIDE_RIGHT = 4;

    private int lowSide = SIDE_BOTTOM;
    private int highSide = SIDE_RIGHT;

    private Vector2 randLow = new Vector2();
    private Vector2 randHigh = new Vector2();

    private Random random = new Random();

    private Rectangle rect = new Rectangle();
    private Vector2 tmp = new Vector2();

    private Array<Vector2> points;

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

        lowPos = new NumericalValue();
        lowSize = new NumericalValue();
        highPos = new NumericalValue();
        highSize = new NumericalValue();
    }

    @Override
    public void processCustomValues () {

        float alpha = this.alpha.getFloat();

        alpha = interpolate(alpha); // apply the curve

        // let's find pos by shape
        getRandomPosOn(lowSide, lowEdge, lowShape, lowPos, lowSize, randLow);
        getRandomPosOn(highSide, highEdge, highShape, highPos, highSize, randHigh);

        float x = Interpolation.linear.apply(randLow.x, randHigh.x, alpha);
        float y = Interpolation.linear.apply(randLow.y, randHigh.y, alpha);

        output.set(x, y);
    }

    private void getRandomPosOn(int side, boolean edge, int shape, NumericalValue pos, NumericalValue size, Vector2 result) {
        random.setSeed((long) ((getScope().getFloat(ScopePayload.PARTICLE_SEED) * 10000 * index * 1000)));
        float angle = random.nextFloat();

        if(side == SIDE_TOP) angle = angle/2f;
        if(side == SIDE_BOTTOM) angle = angle/2f + 0.5f;
        if(side == SIDE_LEFT) angle = angle/2f + 0.25f;
        if(side == SIDE_RIGHT) angle = angle/2f - 0.25f;

        if(shape == TYPE_SQUARE) {
            findRandomSquarePos(angle, pos, size, result);
        } else if (shape == TYPE_ELLIPSE) {
            findRandomEllipsePos(angle, pos, size, result);
        } else if (shape == TYPE_LINE){
            findRandomLinePos(angle, pos, size, result);
        }

        float endX = result.x + tolerance;
        float endY = result.y + tolerance;
        float startX = result.x - tolerance;
        float startY = result.y - tolerance;
        if(!edge) {
            startX = pos.get(0);
            startY = pos.get(1);
        }

        float resultX = random.nextFloat() * (endX - startX) + startX;
        float resultY = random.nextFloat() * (endY - startY) + startY;

        result.set(resultX, resultY);
    }

    private void findRandomEllipsePos(float angle, NumericalValue pos, NumericalValue size, Vector2 result) {
        angle = angle * 360;

        float x = MathUtils.cosDeg(angle) * size.get(0)/2f + pos.get(0);
        float y = MathUtils.sinDeg(angle) * size.get(1)/2f + pos.get(1);

        result.set(x, y);
    }

    private void findRandomSquarePos(float angle, NumericalValue pos, NumericalValue size, Vector2 result) {
        angle = angle * 360;
        rect.set(pos.get(0) - size.get(0)/2f, pos.get(1) - size.get(1)/2f, size.get(0), size.get(1));
        intersectSegmentRectangle(pos.get(0), pos.get(1), pos.get(0) + rect.width * MathUtils.cosDeg(angle), pos.get(1) + rect.height * MathUtils.sinDeg(angle), rect, result);
    }

    private void findRandomLinePos(float angle, NumericalValue pos, NumericalValue size, Vector2 result) {
        angle = angle * 360;
        rect.set(pos.get(0) - size.get(0)/2f, pos.get(1) - size.get(1)/2f, size.get(0), size.get(1));
        tmp.set(rect.width, rect.height); // initial segment vector; for alpha
        float alpha = tmp.angle();
        float beta = angle - alpha;
        float r3 = tmp.set(MathUtils.cosDeg(angle) * rect.width/2f, MathUtils.sinDeg(angle) * rect.height/2f).len();
        float r4 = MathUtils.cosDeg(beta) * r3;
        float posX = r4 * MathUtils.cosDeg(alpha) + pos.get(0);
        float posY = r4 * MathUtils.sinDeg(alpha) + pos.get(1);

        result.set(posX, posY);
    }

    public static boolean intersectSegmentRectangle (float startX, float startY, float endX, float endY, Rectangle rectangle, Vector2 intersection) {
        float rectangleEndX = rectangle.x + rectangle.width;
        float rectangleEndY = rectangle.y + rectangle.height;

        if (Intersector.intersectSegments(startX, startY, endX, endY, rectangle.x, rectangle.y, rectangle.x, rectangleEndY, intersection)) return true;

        if (Intersector.intersectSegments(startX, startY, endX, endY, rectangle.x, rectangle.y, rectangleEndX, rectangle.y, intersection)) return true;

        if (Intersector.intersectSegments(startX, startY, endX, endY, rectangleEndX, rectangle.y, rectangleEndX, rectangleEndY, intersection))
            return true;

        if (Intersector.intersectSegments(startX, startY, endX, endY, rectangle.x, rectangleEndY, rectangleEndX, rectangleEndY, intersection))
            return true;

        return rectangle.contains(startX, startY);
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

    public void setLowPos(Vector2 pos) {
        lowPos.set(pos.x, pos.y);
    }

    public void getLowPos(Vector2 result) {
        result.set(lowPos.get(0), lowPos.get(1));
    }
    public void getHighPos(Vector2 result) {
        result.set(highPos.get(0), highPos.get(1));
    }
    public void getLowSize(Vector2 result) {
        result.set(lowSize.get(0), lowSize.get(1));
    }
    public void getHighSize(Vector2 result) {
        result.set(highSize.get(0), highSize.get(1));
    }

    public void setLowSize(Vector2 size) {
        lowSize.set(size.x, size.y);
    }

    public void setHighPos(Vector2 pos) {
        highPos.set(pos.x, pos.y);
    }

    public void setHighSize(Vector2 size) {
        highSize.set(size.x, size.y);
    }

    public void setLowShape(int shape) {
        lowShape = shape;
    }

    public void setHighShape(int shape) {
        highShape = shape;
    }

    private void resetPoints() {
        // need to guarantee at least one point
        points = new Array<>();
        Vector2 point = new Vector2(0, 0.5f);
        points.add(point);
    }

    public void setPoints(Array<Vector2> newPoints) {
        points.clear();
        for(Vector2 point : newPoints) {
            Vector2 newPoint = new Vector2(point);
            points.add(newPoint);
        }
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

    public void removePoint(int i) {
        if(points.size > 1) {
            points.removeIndex(i);
        }
    }

    public void setLowEdge(boolean edge) {
        lowEdge = edge;
    }

    public void setHighEdge(boolean edge) {
        highEdge = edge;
    }

    public void setLowSide(int side) {
        lowSide = side;
    }

    public void setHighSide(int side) {
        highSide = side;
    }

    public int getLowShape() {
        return lowShape;
    }

    public int getHighShape() {
        return highShape;
    }

    public boolean getLowEdge() {
        return lowEdge;
    }

    public boolean getHighEdge() {
        return highEdge;
    }

    public int getLowSide() {
        return lowSide;
    }

    public int getHighSide() {
        return highSide;
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);

        points.clear();
        final JsonValue points = jsonData.get("points");
        for (JsonValue point : points) {
            createPoint(point.get(0).asFloat(), point.get(1).asFloat());
        }

        final JsonValue low = jsonData.get("low");
        final JsonValue high = jsonData.get("high");

        lowEdge = low.getBoolean("edge");
        highEdge = high.getBoolean("edge");

        lowShape = low.getInt("shape");
        highShape = high.getInt("shape");

        lowSide = low.getInt("side");
        highSide = high.getInt("side");

        final JsonValue lowRect = low.get("rect");
        final JsonValue highRect = high.get("rect");

        lowPos.set(lowRect.getFloat("x"), lowRect.getFloat("y"));
        lowSize.set(lowRect.getFloat("width"), lowRect.getFloat("height"));

        highPos.set(highRect.getFloat("x"), highRect.getFloat("y"));
        highSize.set(highRect.getFloat("width"), highRect.getFloat("height"));
    }

    @Override
    public void write(Json json) {
        super.write(json);

        json.writeArrayStart("points");
        for (Vector2 point : getPoints()) {
            json.writeObjectStart();
            json.writeValue("x", point.x);
            json.writeValue("y", point.y);
            json.writeObjectEnd();
        }
        json.writeArrayEnd();

        json.writeObjectStart("low");
        json.writeValue("edge", lowEdge);
        json.writeValue("shape", lowShape);
        json.writeValue("side", lowSide);
        json.writeObjectStart("rect");
        json.writeValue("x", lowPos.get(0));
        json.writeValue("y", lowPos.get(1));
        json.writeValue("width", lowSize.get(0));
        json.writeValue("height", lowSize.get(1));
        json.writeObjectEnd();
        json.writeObjectEnd();

        json.writeObjectStart("high");
        json.writeValue("edge", highEdge);
        json.writeValue("shape", highShape);
        json.writeValue("side", highSide);
        json.writeObjectStart("rect");
        json.writeValue("x", highPos.get(0));
        json.writeValue("y", highPos.get(1));
        json.writeValue("width", highSize.get(0));
        json.writeValue("height", highSize.get(1));
        json.writeObjectEnd();
        json.writeObjectEnd();
    }
}
