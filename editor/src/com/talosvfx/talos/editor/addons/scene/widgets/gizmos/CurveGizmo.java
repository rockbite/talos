package com.talosvfx.talos.editor.addons.scene.widgets.gizmos;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.FocusManager;
import com.talosvfx.talos.editor.addons.scene.logic.componentwrappers.CurveComponentWrapper;
import com.talosvfx.talos.editor.project2.SharedResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CurveGizmo extends Gizmo {

    private static final Logger logger = LoggerFactory.getLogger(CurveGizmo.class);
    private Bezier<Vector2> bezier = new Bezier<>();
    private Vector2 tmp = new Vector2();
    private Vector2 tmp2 = new Vector2();
    private Vector2 tmp3 = new Vector2();
    private Vector2 tmp4 = new Vector2();

    private int animatingAnchor = -1;
    private Vector2 ctrlOne = new Vector2();
    private Vector2 ctrlTwo = new Vector2();

    private int touchedPointIndex;
    private Vector2 touchedPointRef;

    private int selectedSegmentIndex = -1;

    private Color darkerLine = Color.valueOf("#333333");

    private Actor animateActor;

    public CurveGizmo() {
        animateActor = new Actor();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {

        if(gameObject.hasComponent(CurveComponentWrapper.class)) {
            CurveComponentWrapper curve = gameObject.getComponent(CurveComponentWrapper.class);
            Array<Vector2> points = curve.points;

            if(selected) {
                for (int i = 0; i < points.size; i++) {
                    if(i % 3 == 0) {
                        drawCircle(toWorld(points.get(i)), batch);
                    }
                }
            }

            for(int i = 0; i < curve.getNumSegments(); i++) {
                Vector2[] pointsInSegment = curve.getPointsInSegment(i);

                if(selected) {
                    Color color = Color.GRAY;
                    if(curve.automaticControl){
                        color = darkerLine;
                    }

                    ctrlOne.set(pointsInSegment[1]);
                    ctrlTwo.set(pointsInSegment[2]);

                    if(animatingAnchor >= 0) {
                        if (i * 3 + 2 == animatingAnchor - 1) {
                            tmp.set(ctrlTwo).sub(curve.points.get(animatingAnchor)).scl(animateActor.getX()).add(curve.points.get(animatingAnchor));
                            ctrlTwo.set(tmp);
                        } else if (i * 3 + 1 == animatingAnchor + 1) {
                            tmp.set(ctrlOne).sub(curve.points.get(animatingAnchor)).scl(animateActor.getX()).add(curve.points.get(animatingAnchor));
                            ctrlOne.set(tmp);
                        }

                    }

                    drawLine(batch, toWorld(ctrlOne, tmp2), toWorld(pointsInSegment[0], tmp3), color);
                    drawLine(batch, toWorld(ctrlTwo, tmp2), toWorld(pointsInSegment[3], tmp3), color);

                    if(!curve.automaticControl) {
                        drawCircle(toWorld(ctrlOne), batch);
                        drawCircle(toWorld(ctrlTwo), batch);
                    }
                }

                bezier.set(pointsInSegment);

                Vector2 prev = bezier.valueAt(tmp, 0);
                float step = 1f/20f;
                for(float t = step; t <= 1f; t+=step) {
                    Vector2 curr = bezier.valueAt(tmp2, t);
                    if(selectedSegmentIndex == i) {
                        drawLine(batch, toWorld(prev, tmp3), toWorld(curr, tmp4), Color.YELLOW);
                    } else {
                        drawLine(batch, toWorld(prev, tmp3), toWorld(curr, tmp4), Color.RED);
                    }
                    prev.set(curr);
                }
                Vector2 curr = bezier.valueAt(tmp2,1f);
                if(selectedSegmentIndex == i) {
                    drawLine(batch, toWorld(prev, tmp3), toWorld(curr, tmp4), Color.YELLOW);
                } else {
                    drawLine(batch, toWorld(prev, tmp3), toWorld(curr, tmp4), Color.RED);
                }
            }
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            FocusManager.resetFocus(SharedResources.stage);
        }
    }

    @Override
    public boolean hit(float x, float y) {
        if(!selected) return false;

        tmp.set(getX(), getY());
        if(isPointHit(tmp, x, y, 30)) return false;

        int touchPoint = getTouchedPoint(x, y);

        if(touchPoint >= 0) {
            return true;
        }

        if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            return true;
        }

        if(Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            return false;
        }

        return true;
    }

    private int getTouchedPoint(float x, float y) {
        CurveComponentWrapper curve = gameObject.getComponent(CurveComponentWrapper.class);
        if(curve != null) {
            Array<Vector2> points = curve.points;
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
        CurveComponentWrapper curve = gameObject.getComponent(CurveComponentWrapper.class);
        int touchedPoint = getTouchedPoint(x, y);
        if(button == 0) {
            if (touchedPoint >= 0) {
                touchedPointRef = gameObject.getComponent(CurveComponentWrapper.class).points.get(touchedPoint);
                touchedPointIndex = touchedPoint;
            } else {
                if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                    // adding new points then
                    curve.addSegment(toLocal(new Vector2(x, y)));
                    touchedPointIndex = curve.points.size - 1;
                    touchedPointRef = curve.points.get(touchedPointIndex);
                } else if (selectedSegmentIndex >= 0) {
                    curve.splitSegment(toLocal(new Vector2(x, y)), selectedSegmentIndex);
                    touchedPointIndex = selectedSegmentIndex * 3 + 3;
                    touchedPointRef = curve.points.get(touchedPointIndex);
                    animateAnchor(touchedPointIndex);
                }
            }
        } else if (button == 1) {
            if(touchedPoint % 3 == 0) {
                curve.deleteSegment(touchedPoint);
            }
        }
    }

    private void animateAnchor(int anchorIndex) {
        animatingAnchor = anchorIndex;

        SharedResources.stage.addActor(animateActor);

        animateActor.clearActions();
        animateActor.setX(0);
        animateActor.addAction(Actions.moveTo(1f, 0f, 1f, Interpolation.elasticOut));
    }

    private float getDistanceToBezier(Bezier<Vector2> bz, Vector2 point) {
        Vector2 prev = bezier.valueAt(tmp3, 0.05f);
        float min = 99999;
        for(float t = 0.05f; t < 0.95f; t+=1/10f) {
            Vector2 curr = bezier.valueAt(tmp2, t);
            float dst = Intersector.distanceSegmentPoint(prev, curr, point);
            if(min > dst) min = dst;
            prev.set(curr);
        }
        return min;
    }

    @Override
    public void mouseMoved(float x, float y) {
        float threshold =  worldPerPixel * 10f;

        selectedSegmentIndex = -1;

        Vector2 local = toLocal(tmp, x, y);
        CurveComponentWrapper curve = gameObject.getComponent(CurveComponentWrapper.class);
        for(int i = 0; i < curve.getNumSegments(); i++) {
            Vector2[] pointsInSegment = curve.getPointsInSegment(i);
            bezier.set(pointsInSegment);
            float dst = getDistanceToBezier(bezier, local);
            if(dst < threshold) {
                selectedSegmentIndex = i;
            }
        }

        if(selectedSegmentIndex >= 0) {
            // debug info here
        }
    }

    @Override
    public void touchDragged(float x, float y) {
        if(touchedPointRef != null) {
            CurveComponentWrapper curve = gameObject.getComponent(CurveComponentWrapper.class);
            Vector2 pos = toLocal(tmp3.set(x, y));
            curve.movePoint(touchedPointIndex, pos.x, pos.y);
        }
    }

    @Override
    public void touchUp(float x, float y) {
        touchedPointRef = null;
        touchedPointIndex = -1;
    }

    private Vector2 toWorld(Vector2 local) {
        return toWorld(local, tmp3);
    }

    private Vector2 toWorld(Vector2 local, Vector2 out) {
        out.set(local);
        out.add(getX(), getY());
        return out;
    }

    private Vector2 toLocal(Vector2 out, float x, float y) {
        out.set(x, y);
        out.sub(getX(), getY());
        return out;
    }

    private Vector2 toLocal(Vector2 world) {
        world.sub(getX(), getY());
        return world;
    }

    @Override
    public boolean catchesShift() {
        return true;
    }
}
