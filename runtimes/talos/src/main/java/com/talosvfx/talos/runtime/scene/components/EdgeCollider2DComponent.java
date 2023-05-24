package com.talosvfx.talos.runtime.scene.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.runtime.scene.ValueProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EdgeCollider2DComponent extends AComponent {

    private static final Logger logger = LoggerFactory.getLogger(EdgeCollider2DComponent.class);

    public Array<Vector2> points = new Array<>();

    public boolean isClosed = false;

    public float scale = 1;

    @ValueProperty(min = 0, max = 2f, step = 0.004f)
    public float edgeRadius = 0f;

    public EdgeCollider2DComponent() {
        setToNew();
    }

    public void setToNew() {
        points.clear();
        points.add(new Vector2(-2, 0).scl(scale));
        points.add(new Vector2(2, 0).scl(scale));
    }

    public void getSegmentPoints(int segmentIndex, Vector2 p1, Vector2 p2) {
        p1.set(points.get(segmentIndex % points.size));
        p2.set(points.get((segmentIndex + 1) % points.size));
    }

    @Override
    public void reset() {
        super.reset();
        setToNew();
    }

    public void scale(float scale) {
        this.scale = scale;
        for (Vector2 point : points) {
            point.scl(scale);
        }
    }

    public void movePoint(int touchedPointIndex, float x, float y) {
        int index = touchedPointIndex % points.size;
        points.get(index).set(x, y);
    }

    public void addSegment(Vector2 pos) {
        points.add(new Vector2(pos));
    }

    public void splitSegment(Vector2 pos, int segmentIndex) {
        Vector2 newPos = new Vector2();
        newPos.set(pos);

        points.insert(segmentIndex, newPos);
    }

    public void deleteSegment(int segmentIndex) {
        points.removeIndex(segmentIndex);
    }
}
