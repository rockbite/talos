package com.talosvfx.talos.runtime.modules;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.ScopePayload;
import com.talosvfx.talos.runtime.values.NumericalValue;

public class ShapeModule extends AbstractModule {

    public static final int ALPHA = 0;
    public static final int OUTPUT = 0;

    NumericalValue alpha;
    NumericalValue output;

    public static final int TYPE_SQUARE = 0;
    public static final int TYPE_ELLIPSE = 1;

    private int shape;
    NumericalValue pos;
    NumericalValue size;

    private Rectangle rect = new Rectangle();
    private Vector2 tmp = new Vector2();
    private Vector2 result = new Vector2();

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

    private float interpolate(float alpha, float x1, float y1, float x2, float y2) {
        if(y1 == y2) return y1;
        if(x1 == x2) return y1;

        tmp.set(x2, y2);
        tmp.sub(x1, y1);
        tmp.scl(alpha);
        tmp.add(x1, y1);

        return tmp.y;
    }

    @Override
    protected void defineSlots() {
        alpha = createInputSlot(ALPHA);

        pos = new NumericalValue();
        size = new NumericalValue();

        output = createOutputSlot(OUTPUT);
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

    @Override
    public void processValues() {
        processAlphaDefaults();

        float alpha = this.alpha.getFloat();

        if(size.isEmpty()) {
            size.set(1, 1, 1, 1);
        }

        // let's find pos by shape
        if(shape == TYPE_SQUARE) {
            findRandomSquarePos(alpha, pos, size, result);
        } else if (shape == TYPE_ELLIPSE) {
            findRandomEllipsePos(alpha, pos, size, result);
        }

        output.set(result.x, result.y);
    }

    public void setPos(Vector2 pos) {
        this.pos.set(pos.x, pos.y);
    }

    public void setSize(Vector2 size) {
        this.size.set(size.x, size.y);
    }


    public void setShape(int shape) {
        this.shape = shape;
    }

    public void getPos(Vector2 pos) {
        pos.set(this.pos.get(0), this.pos.get(1));
    }

    public void getSize(Vector2 size) {
        size.set(this.size.get(0), this.size.get(1));
    }

    public int getShape() {
        return shape;
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);

        JsonValue shapeData = jsonData.get("shapeData");
        shape = shapeData.getInt("shape");
        final JsonValue rect = shapeData.get("rect");
        pos.set(rect.getFloat("x"), rect.getFloat("y"));
        size.set(rect.getFloat("width"), rect.getFloat("height"));
    }

    @Override
    public void write(Json json) {
        super.write(json);


        json.writeObjectStart("shapeData");
        json.writeValue("shape", shape);
        json.writeObjectStart("rect");
        json.writeValue("x", pos.get(0));
        json.writeValue("y", pos.get(1));
        json.writeValue("width", size.get(0));
        json.writeValue("height", size.get(1));
        json.writeObjectEnd();
        json.writeObjectEnd();
    }
}
