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
import com.talosvfx.talos.editor.addons.scene.SceneUtils;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.runtime.scene.components.CurveComponent;
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

        if(gameObject.hasComponent(CurveComponent.class)) {
            CurveComponent curve = gameObject.getComponent(CurveComponent.class);
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
//        if(!selected) return false;
        CurveComponent curve = gameObject.getComponent(CurveComponent.class);


        tmp.set(getX(), getY());
//        if(isPointHit(tmp, x, y, 30)) return false;
        if (getClosestWorldPoint(tmp2, x, y, 20f)) {
            return true;
        }

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

        return false;
    }

    private int getTouchedPoint(float x, float y) {
        CurveComponent curve = gameObject.getComponent(CurveComponent.class);
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
        CurveComponent curve = gameObject.getComponent(CurveComponent.class);
        int touchedPoint = getTouchedPoint(x, y);
        if(button == 0) {
            if (touchedPoint >= 0) {
                touchedPointRef = gameObject.getComponent(CurveComponent.class).points.get(touchedPoint);
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

    @Override
    public void setSelected (boolean selected) {
        super.setSelected(selected);
    }

    private float getDistanceToBezier(Bezier<Vector2> bz, Vector2 point, Vector2 closestOut, int samples) {
        float alphaIterate = 1f/samples;
        Vector2 prev = bz.valueAt(tmp3, alphaIterate);
        float min = Float.MAX_VALUE;
        for(float t = alphaIterate; t < 1; t+=alphaIterate) {
            Vector2 curr = bz.valueAt(tmp2, t);
            float dst = Intersector.distanceSegmentPoint(prev, curr, point);
            if(min > dst) {
                min = dst;
                closestOut.set(curr);
            }
            prev.set(curr);
        }
        return min;
    }

    @Override
    public void mouseMoved(float x, float y) {
        float threshold =  worldPerPixel * 10f;

        selectedSegmentIndex = -1;

        Vector2 local = toLocal(tmp, x, y);
        CurveComponent curve = gameObject.getComponent(CurveComponent.class);
        for(int i = 0; i < curve.getNumSegments(); i++) {
            Vector2[] pointsInSegment = curve.getPointsInSegment(i);
            bezier.set(pointsInSegment);
            float dst = getDistanceToBezier(bezier, local, tmp4, 1000);
            if(dst < threshold) {
                selectedSegmentIndex = i;
            }
        }

        if(selectedSegmentIndex >= 0) {
            // debug info here
        }
    }

    private boolean getClosestWorldPoint (Vector2 out, float worldX, float worldY, float threshHold) {
        float minDist = Float.MAX_VALUE;
        int chosenSegment = 0;
        float closestWorldX = 0;
        float closestWorldY = 0;


        Vector2 local = toLocal(tmp, worldX, worldY);

        float localX = local.x;
        float localY = local.y;

        CurveComponent curve = gameObject.getComponent(CurveComponent.class);
        for(int i = 0; i < curve.getNumSegments(); i++) {
            Vector2[] pointsInSegment = curve.getPointsInSegment(i);
            bezier.set(pointsInSegment);

            local.set(localX, localY);

            float distanceToBezier = getDistanceToBezier(bezier, local, tmp4, 1000);
            if (distanceToBezier < minDist) {
                minDist = distanceToBezier;
                closestWorldX = tmp4.x;
                closestWorldY = tmp4.y;
            }
        }

        out.set(closestWorldX, closestWorldY);
        Vector2 world = toWorld(out);


        return isPointHit(world, worldX, worldY, threshHold);
    }


    private boolean touchDragged;
    @Override
    public void touchDragged(float x, float y) {
        if(touchedPointRef != null) {
            touchDragged = true;

            final CurveComponent curve = gameObject.getComponent(CurveComponent.class);

            Vector2 pos = toLocal(tmp3.set(x, y));
            curve.movePoint(touchedPointIndex, pos.x, pos.y);

            SceneUtils.componentUpdated(curve.getGameObject().getGameObjectContainerRoot(), curve.getGameObject(), curve, true);
        }
    }

    @Override
    public void touchUp(float x, float y) {
        touchedPointRef = null;
        touchedPointIndex = -1;

        if (touchDragged) {
            touchDragged = false;
            final CurveComponent curve = gameObject.getComponent(CurveComponent.class);
            SceneUtils.componentUpdated(curve.getGameObject().getGameObjectContainerRoot(), curve.getGameObject(), curve, false);
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
