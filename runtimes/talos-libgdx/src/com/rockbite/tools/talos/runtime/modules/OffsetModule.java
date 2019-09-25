package com.rockbite.tools.talos.runtime.modules;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;
import com.rockbite.tools.talos.runtime.ScopePayload;
import com.rockbite.tools.talos.runtime.values.NumericalValue;

import java.util.Comparator;
import java.util.Random;

public class OffsetModule extends Module {

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

    private int side = 0;
    private float tolerance = 0;

    public static final int TYPE_SQUARE = 0;
    public static final int TYPE_ELLIPSE = 1;
    public static final int TYPE_LINE = 2;

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
        alpha = createInputSlot(ALPHA);
        output = createOutputSlot(OUTPUT);

        lowPos = new NumericalValue();
        lowSize = new NumericalValue();
        highPos = new NumericalValue();
        highSize = new NumericalValue();
    }

    @Override
    public void processValues() {
        processAlphaDefaults();

        float alpha = this.alpha.getFloat();

        alpha = interpolate(alpha); // apply the curve

        // let's find pos by shape
        getRandomPosOn(lowEdge, lowShape, lowPos, lowSize, randLow);
        getRandomPosOn(highEdge, highShape, highPos, highSize, randHigh);

        float x = Interpolation.linear.apply(randLow.x, randHigh.x, alpha);
        float y = Interpolation.linear.apply(randLow.y, randHigh.y, alpha);

        output.set(x, y);
    }

    private void getRandomPosOn(boolean edge, int shape, NumericalValue pos, NumericalValue size, Vector2 result) {
        random.setSeed((long) ((getScope().getFloat(ScopePayload.PARTICLE_SEED) * 10000 * index * 1000)));
        float angle = random.nextFloat();
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

        float x = MathUtils.cosDeg(angle) * size.get(0) + pos.get(0);
        float y = MathUtils.sinDeg(angle) * size.get(1) + pos.get(1);

        result.set(x, y);
    }

    private void findRandomSquarePos(float angle, NumericalValue pos, NumericalValue size, Vector2 result) {
        angle = angle * 360;
        rect.set(pos.get(0) - size.get(0)/2f, pos.get(1) - size.get(1)/2f, size.get(0), size.get(1));
        intersectSegmentRectangle(pos.get(0), pos.get(1), pos.get(0) + rect.width * MathUtils.cosDeg(angle), pos.get(1) + rect.height * MathUtils.sinDeg(angle), rect, result);
    }

    private void findRandomLinePos(float angle, NumericalValue pos, NumericalValue size, Vector2 result) {

    }

    protected void processAlphaDefaults() {
        if(alpha.isEmpty()) {
            // as default we are going to fetch the lifetime or duration depending on context
            float requester = getScope().getFloat(ScopePayload.REQUESTER_ID);
            if(requester < 1) {
                // particle
                alpha.set(getScope().get(ScopePayload.PARTICLE_ALPHA));
                alpha.setEmpty(false);
            } else if(requester > 1) {
                // emitter
                alpha.set(getScope().get(ScopePayload.EMITTER_ALPHA));
                alpha.setEmpty(false);
            } else {
                // whaat?
                alpha.set(0);
            }
        }
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
}
