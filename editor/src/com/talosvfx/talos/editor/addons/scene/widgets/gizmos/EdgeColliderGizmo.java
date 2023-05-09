package com.talosvfx.talos.editor.addons.scene.widgets.gizmos;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.FocusManager;
import com.talosvfx.talos.editor.addons.scene.SceneUtils;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.runtime.scene.components.CurveComponent;
import com.talosvfx.talos.runtime.scene.components.EdgeCollider2DComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EdgeColliderGizmo extends Gizmo {

    private static final Logger logger = LoggerFactory.getLogger(EdgeColliderGizmo.class);

    private Vector2 tmp4 = new Vector2();
    private Vector2 tmp5 = new Vector2();
    private int touchedPointIndex;
    private Vector2 touchedPointRef;

    private int selectedSegmentIndex = -1;

    private Color lineColor = Color.valueOf("#a891c8");
    private Color selectedLineColor = Color.valueOf("#dccdef");


    @Override
    public void draw(Batch batch, float parentAlpha) {

        if(gameObject.hasComponent(EdgeCollider2DComponent.class)) {
            EdgeCollider2DComponent component = gameObject.getComponent(EdgeCollider2DComponent.class);
            Array<Vector2> points = component.points;

            if(selected) {
                for (int i = 0; i < points.size; i++) {
                    drawCircle(toWorld(points.get(i)), batch);
                }
            }


            for (int i = 0; i < points.size; i++) {
                if(i < points.size - 1) {
                    drawSegment(batch, i);
                } else {
                    if(component.isClosed) {
                        drawSegment(batch, i);
                    }
                }

                if(component.edgeRadius > 0) {
                    drawCircle(toWorld(points.get(i)), batch, component.edgeRadius, lineColor, 1f);
                }
            }
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            FocusManager.resetFocus(SharedResources.stage);
        }
    }

    private void drawSegment(Batch batch, int segmentIndex) {
        EdgeCollider2DComponent component = gameObject.getComponent(EdgeCollider2DComponent.class);
        component.getSegmentPoints(segmentIndex, tmp4, tmp5);
        Color color = lineColor;
        if(segmentIndex == selectedSegmentIndex) {
            color = selectedLineColor;
        }
        drawLine(batch, toWorld(tmp4, tmp2), toWorld(tmp5, tmp3), color);

        if(component.edgeRadius > 0) {

        }
    }


    @Override
    public boolean hit(float x, float y) {
        tmp.set(getX(), getY());

        int touchPoint = getTouchedPoint(x, y);

        if (getClosestWorldPoint(tmp2, x, y, worldPerPixel * 20f)) {
            return true;
        }

        if(touchPoint >= 0) {
            return true;
        }

        if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            return true;
        }

        if(Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            return false;
        }

        return false;
    }

    private boolean getClosestWorldPoint(Vector2 out, float x, float y, float threshold) {
        float minDst = Float.MAX_VALUE;
        Vector2 local = toLocal(tmp, x, y);
        EdgeCollider2DComponent component = gameObject.getComponent(EdgeCollider2DComponent.class);
        Array<Vector2> points = component.points;
        for(int i = 0; i < points.size; i++) {
            if(!component.isClosed && i >= points.size - 1) break;

            component.getSegmentPoints(i, tmp4, tmp5);
            Intersector.nearestSegmentPoint(tmp4, tmp5, local, tmp3); // tmp3 contains nearest point
            float dst = tmp3.dst(local);

            if(minDst > dst) {
                minDst = dst;
                out.set(tmp3);
            }
        }


        return minDst < threshold;
    }

    private int getTouchedPoint(float x, float y) {
        EdgeCollider2DComponent component = gameObject.getComponent(EdgeCollider2DComponent.class);
        if(component != null) {
            Array<Vector2> points = component.points;
            for (int i = 0; i < points.size; i++) {
                if (isPointHit(toWorld(points.get(i)), x, y)) {
                    return i;
                }
            }
        }

        return -1;
    }

    private boolean isPointHit(Vector2 point, float x, float y, float radius) {
        float dst = point.dst(x, y) / worldPerPixel;
        if (dst < radius) {
            return true;
        }

        return false;
    }

    private boolean isPointHit(Vector2 point, float x, float y) {
        return isPointHit(point, x, y, 25);
    }

    @Override
    public void touchDown(float x, float y, int button) {
        EdgeCollider2DComponent component = gameObject.getComponent(EdgeCollider2DComponent.class);
        int touchedPoint = getTouchedPoint(x, y);
        if(button == 0) {
            if (touchedPoint >= 0) {
                touchedPointRef = gameObject.getComponent(EdgeCollider2DComponent.class).points.get(touchedPoint);
                touchedPointIndex = touchedPoint;
            } else {
                if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                    // adding new points then
                    component.addSegment(toLocal(new Vector2(x, y)));
                    touchedPointIndex = component.points.size - 1;
                    touchedPointRef = component.points.get(touchedPointIndex);
                } else if (selectedSegmentIndex >= 0) {
                    selectedSegmentIndex++;
                    component.splitSegment(toLocal(new Vector2(x, y)), selectedSegmentIndex);
                    touchedPointIndex = selectedSegmentIndex;
                    touchedPointRef = component.points.get(touchedPointIndex);
                }
            }
        } else if (button == 1) {
            if(touchedPoint >= 0) {
                component.deleteSegment(touchedPoint);
            }
        }
    }


    @Override
    public void mouseMoved(float x, float y) {
        float threshold =  worldPerPixel * 10f;

        selectedSegmentIndex = -1;

        Vector2 local = toLocal(tmp, x, y);
        EdgeCollider2DComponent component = gameObject.getComponent(EdgeCollider2DComponent.class);
        Array<Vector2> points = component.points;
        for(int i = 0; i < points.size; i++) {
            if(!component.isClosed && i >= points.size - 1) break;

            component.getSegmentPoints(i, tmp4, tmp5);
            Intersector.nearestSegmentPoint(tmp4, tmp5, local, tmp3); // tmp3 contains nearest point
            float dst = tmp3.dst(local);

            if(dst < threshold) {
                selectedSegmentIndex = i;
            }
        }

        if(selectedSegmentIndex >= 0) {
            // debug info here
        }
    }



    private boolean touchDragged;
    @Override
    public void touchDragged(float x, float y) {
        if(touchedPointRef != null) {
            touchDragged = true;

            final EdgeCollider2DComponent component = gameObject.getComponent(EdgeCollider2DComponent.class);
            Vector2 pos = toLocal(tmp3.set(x, y));
            component.movePoint(touchedPointIndex, pos.x, pos.y);
            SceneUtils.componentUpdated(component.getGameObject().getGameObjectContainerRoot(), component.getGameObject(), component, true);
        }
    }

    @Override
    public void touchUp(float x, float y) {
        touchedPointRef = null;
        touchedPointIndex = -1;

        if (touchDragged) {
            touchDragged = false;
            final EdgeCollider2DComponent component = gameObject.getComponent(EdgeCollider2DComponent.class);
            SceneUtils.componentUpdated(component.getGameObject().getGameObjectContainerRoot(), component.getGameObject(), component, false);
        }
    }

    @Override
    public boolean catchesShift() {
        return true;
    }

    @Override
    public int getPriority () {
        return -1;
    }
}
