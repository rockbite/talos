package com.talosvfx.talos.editor.addons.scene.widgets.gizmos;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.events.ComponentUpdated;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.SpriteRendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;

public class SmartTransformGizmo extends Gizmo {

    private static final Logger logger = LoggerFactory.getLogger(SmartTransformGizmo.class);
    private final Image rect;
    private final Image circle;
    protected final NinePatch rectPatch;
    protected float prevScale = 1f;

    protected Vector2[] points = new Vector2[4];
    private Vector2[] rotationAreas = new Vector2[4];

    protected static final int LB = 0;
    protected static final int LT = 1;
    protected static final int RT = 2;
    protected static final int RB = 3;
    protected int touchedPoint = -1;
    private int touchedRA = -1;

    private Vector2 touchedDownLocation = new Vector2();
    private Vector2 prevDragLocation = new Vector2();


    protected Vector2 tmp = new Vector2();
    private Vector2 tmp2 = new Vector2();
    private Vector2 tmp3 = new Vector2();
    private Vector2 tmp4 = new Vector2();

    protected Vector2[] prevPoints = new Vector2[4];
    protected Vector2[] nextPoints = new Vector2[4];
    private float prevRotation;
    private float nextRotation;

    public SmartTransformGizmo() {
        NinePatchDrawable rectDrawable = (NinePatchDrawable) SharedResources.skin.getDrawable("border-dark-orange");
        rect = new Image(rectDrawable);
        rectPatch = rectDrawable.getPatch();

        circle = new Image(SharedResources.skin.getDrawable("vfx-green"));

        for (int i = 0; i < 4; i++) {
            points[i] = new Vector2();
            prevPoints[i] = new Vector2();
            nextPoints[i] = new Vector2();
            rotationAreas[i] = new Vector2();
        }
    }

    @Override
    public void setGameObject (GameObject gameObject) {
        super.setGameObject(gameObject);

        updatePointsFromComponent();
    }

    @Override
    public void act (float delta) {
        super.act(delta);
        updatePointsFromComponent();
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
        if(!selected) return;
        rectPatch.scale(1f/ prevScale, 1f/prevScale);
        rectPatch.scale(worldPerPixel, worldPerPixel);
        prevScale = worldPerPixel;

        if(gameObject.hasComponent(TransformComponent.class)) {
            for (int i = 0; i < 4; i++) {
                drawCircle(points[i], batch);
            }

            // lets draw lines
            drawLine(batch, points[LB], points[LT], ColorLibrary.ORANGE);
            drawLine(batch, points[LT], points[RT], ColorLibrary.ORANGE);
            drawLine(batch, points[RT], points[RB], ColorLibrary.ORANGE);
            drawLine(batch, points[RB], points[LB], ColorLibrary.ORANGE);
        }

    }

    protected Vector2 getWorldLocAround(Vector2 point, float x, float y) {
        point.set(x, y);
        TransformComponent.localToWorld(gameObject, point);

        return point;
    }

    protected void drawCircle(Vector2 pos, Batch batch) {
        float size = 20 * worldPerPixel; // pixel
        circle.setSize(size, size);
        circle.setPosition(pos.x - size/2f, pos.y-size/2f);
        circle.draw(batch, 1f);
    }

    @Override
    public boolean hit (float x, float y) {
        if(!selected) return false;

        if (isOnTouchedPoint(x, y)) {
            return true;
        }
        if (isOnTouchedRotationArea(x, y)) {
            return true;
        }

        return false;
    }

    protected boolean isOnTouchedRotationArea (float x, float y) {
        int touchedRA = getTouchedRotationArea(x, y);

        if(touchedRA >= 0) {
            return true;
        }
        return false;
    }
    protected boolean isOnTouchedPoint (float x, float y) {
        int touchedPoint = getTouchedPoint(x, y);
        if(touchedPoint >= 0) {
            return true;
        }
        return false;
    }

    private int getTouchedRotationArea(float x, float y) {
        for (int i = 0; i < 4; i++) {
            if (isRotationAreaHit(rotationAreas[i], x, y)) {
                return i;
            }
        }

        return -1;
    }

    private int getTouchedPoint(float x, float y) {
        for (int i = 0; i < 4; i++) {
            if (isPointHit(points[i], x, y)) {
                return i;
            }
        }

        return -1;
    }

    private boolean isPointHit(Vector2 point, float x, float y) {
        float dst = point.dst(x, y) / worldPerPixel;
        if (dst < 25) {
            return true;
        }

        return false;
    }

    private boolean isRotationAreaHit(Vector2 rotationArea, float x, float y) {
        float dst = rotationArea.dst(x, y) / worldPerPixel;
        if (dst < 30) {
            return true;
        }

        return false;
    }

    @Override
    public void touchDown (float x, float y, int button) {
        touchedPoint = getTouchedPoint(x, y);

        touchedRA = getTouchedRotationArea(x, y);

        touchedDownLocation.set(x, y);
        prevDragLocation.set(x, y);
    }

    @Override
    public void touchDragged (float x, float y) {

        if(touchedPoint >= 0) {
            setNewPointValue(touchedPoint, x, y);
            updatePointsFromComponent();
            reportResizeUpdated(true);
        } else if (touchedRA >= 0) {
            applyRotationChange(x, y);
            updateRotationAreas(tmp.x, tmp.y);
            reportResizeUpdated(true);
        }

        prevDragLocation.set(x, y);
    }

    @Override
    public void touchUp (float x, float y) {
        if(touchedPoint >= 0) {
            setNewPointValue(touchedPoint, x, y);
            updatePointsFromComponent();
            reportResizeUpdated(false);
        } else if (touchedRA >= 0) {
            applyRotationChange(x, y);
            updateRotationAreas(tmp.x, tmp.y);
            reportResizeUpdated(false);
        }
    }

    protected void reportResizeUpdated(boolean isRapid) {
        TransformComponent transform = gameObject.getComponent(TransformComponent.class);
        Notifications.fireEvent(Notifications.obtainEvent(ComponentUpdated.class).set(transform, isRapid));
    }

    private void applyRotationChange(float x, float y) {
        tmp.set(points[RT]).sub(points[LB]).scl(0.5f).add(points[LB]); // midpoint
        tmp2.set(prevDragLocation).sub(tmp);
        tmp3.set(x, y).sub(tmp);
        float a1 = tmp2.angleDeg();
        float a2 = tmp3.angleDeg();
        float angleDiff = a2 - a1;
        TransformComponent transform = gameObject.getComponent(TransformComponent.class);
        transform.rotation += angleDiff;
    }

    protected void updatePointsFromComponent () {
        getWorldLocAround(points[LB], -0.5f, -0.5f);
        getWorldLocAround(points[LT],-0.5f, 0.5f);
        getWorldLocAround(points[RT],0.5f, 0.5f);
        getWorldLocAround(points[RB],0.5f, -0.5f);

        tmp.set(points[RT]).sub(points[LB]).scl(0.5f).add(points[LB]); // midpoint
        updateRotationAreas(tmp.x, tmp.y);
    }

    /**
     * @param x of midpoint
     * @param y of midpoint
     */
    protected void updateRotationAreas (float x, float y) {
        float radius = 30 * worldPerPixel;

        for(int i = 0; i < 4; i++) {
            Vector2 point = points[i];

            tmp.set(point);
            tmp.sub(x, y);
            tmp2.set(tmp);
            tmp2.nor();
            tmp2.scl(radius);
            tmp.add(tmp2);
            tmp.add(x, y);

            rotationAreas[i].set(tmp);
        }
    }

    private void setNewPointValue (int touchedPoint, float x, float y) {
        setRectFromPoints(prevPoints);
        prevRotation = getRotation(prevPoints);

        SpriteRendererComponent transform = gameObject.getComponent(SpriteRendererComponent.class);
        float aspect = 1f;
        if (transform != null) {
            aspect = Math.abs(transform.size.x / transform.size.y);
        }

        // tmp2 contains movement diff
        tmp2.set(x, y).sub(points[touchedPoint]);

        // find midpoint
        tmp3.set(points[RT]).sub(points[LB]).scl(0.5f).add(points[LB]);

        for(int i = 0; i < 4; i++) {
            points[i].sub(tmp3);
            points[i].rotateDeg(-prevRotation);
        }
        tmp2.rotateDeg(-prevRotation);
        if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            tmp2.x = tmp2.y * aspect;
            if (touchedPoint == LT || touchedPoint == RB) {
                tmp2.x *= -1;
            }
        }

        points[touchedPoint].add(tmp2); // apply diff

        if(touchedPoint == LB) {
            points[LT].x = points[LB].x;
            points[RB].y = points[LB].y;
        }
        if(touchedPoint == LT) {
            points[RT].y = points[LT].y;
            points[LB].x = points[LT].x;
        }
        if(touchedPoint == RB) {
            points[LB].y = points[RB].y;
            points[RT].x = points[RB].x;
        }
        if(touchedPoint == RT) {
            points[LT].y = points[RT].y;
            points[RB].x = points[RT].x;
        }

        //rotate back
        for(int i = 0; i < 4; i++) {
            points[i].rotateDeg(prevRotation);
            points[i].add(tmp3);
        }

        setRectFromPoints(nextPoints);
        nextRotation = getRotation(nextPoints);

        transformOldToNew();
    }

    private float getPintsAngle(Vector2 p2, Vector2 p1) {
        return Math.round(tmp3.set(p2).sub(p1).angleDeg());
    }

    protected void transformOldToNew () {
        TransformComponent transform = gameObject.getComponent(TransformComponent.class);

        float xDiff = getPintsAngle(nextPoints[RT], nextPoints[LT]) - getPintsAngle(prevPoints[RT], prevPoints[LT]);
        float yDiff = getPintsAngle(nextPoints[LT], nextPoints[LB]) - getPintsAngle(prevPoints[LT], prevPoints[LB]);

        // bring old next points to local space
        for(int i = 0; i < 4; i++) {
            TransformComponent.worldToLocal(gameObject.parent, nextPoints[i]);
        }

        float xSign = transform.scale.x < 0 ? -1: 1;
        float ySign = transform.scale.y < 0 ? -1: 1;

        transform.scale.set(nextPoints[RB].dst(nextPoints[LB]) * xSign, nextPoints[LB].dst(nextPoints[LT]) * ySign);

        if(xDiff != 0) {
            transform.scale.x = transform.scale.x * -1f;
        }
        if(yDiff != 0) {
            transform.scale.y = transform.scale.y * -1f;
        }

        transform.scale = lowerPrecision(transform.scale);
        tmp.set(nextPoints[RT]).sub(nextPoints[LB]).scl(0.5f).add(nextPoints[LB]); // this is midpoint

        if(!Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            transform.position.set(tmp); // lol, this is a funny story actually
        }

        transform.position = lowerPrecision(transform.position);
    }

    protected Vector2 lowerPrecision(Vector2 vec) {
        vec.x = lowerPrecision(vec.x);
        vec.y = lowerPrecision(vec.y);

        return vec;
    }

    protected float lowerPrecision(float number) {
        number *= 10000;
        number = (int)number;
        number /= 10000;

        return number;
    }

    private float getRotation (Vector2[] pointArray) {
        return tmp.set(pointArray[RT]).sub(pointArray[LT]).angleDeg();
    }
    private void setRectFromPoints (Vector2[] pointArray) {
        for (int i = 0; i < 4; i++) {
            pointArray[i].set(points[i]);
        }
    }

    @Override
    public void keyDown (InputEvent event, int keycode) {

        if(keycode == Input.Keys.DOWN && Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            moveInLayerOrder(gameObject, -1);
        }

        if(keycode == Input.Keys.UP && Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            moveInLayerOrder(gameObject, 1);
        }

    }

    public static int getLatestFreeOrderingIndex (String sortingLayer) {
        logger.info("todo find game objects and get free order index");
//        Array<GameObject> list = SceneEditorAddon.get().workspace.getRootGO().getChildrenByComponent(SpriteRendererComponent.class, new Array<>());
//        return list.size;

        return 0;
    }

    protected void moveInLayerOrder (GameObject gameObject, int direction) {
        // direction -1 for down, 1 for up
        if(gameObject.hasComponent(SpriteRendererComponent.class)) {
            SpriteRendererComponent component = gameObject.getComponent(SpriteRendererComponent.class);
            String sortingLayer = component.sortingLayer;

            logger.info("Redo move in layer order");
//            Array<GameObject> list = SceneEditorAddon.get().workspace.getRootGO().getChildrenByComponent(SpriteRendererComponent.class, new Array<>());
//            for(int i = list.size - 1; i >= 0; i--) {
//                if(!list.get(i).getComponent(SpriteRendererComponent.class).sortingLayer.equals(sortingLayer)) {
//                    list.removeIndex(i);
//                }
//                if (list.get(i) == gameObject) {
//                    list.removeIndex(i);
//                }
//            }

//            list.sort(new Comparator<GameObject>() {
//                @Override
//                public int compare (GameObject o1, GameObject o2) {
//                    SpriteRendererComponent o1SpriteRender = o1.getComponent(SpriteRendererComponent.class);
//                    SpriteRendererComponent o2SpriteRender = o2.getComponent(SpriteRendererComponent.class);
//                    return Integer.compare(o1SpriteRender.orderingInLayer, o2SpriteRender.orderingInLayer);
//                }
//            });

//            if (list.size > 0) {
//                int currentOrderInLayer = component.orderingInLayer;
//
//                GameObject objectToSwap = null;
//                for (int i = 0; i < list.size; i++) {
//                    GameObject gameObjectToTest = list.get(i);
//                    int otherOrderInLayer = gameObjectToTest.getComponent(SpriteRendererComponent.class).orderingInLayer;
//                    if ((currentOrderInLayer + direction) == otherOrderInLayer) {
//                        objectToSwap = gameObjectToTest;
//                        break;
//                    }
//                }
//                if (objectToSwap != null) {
//                    int otherOrder = objectToSwap.getComponent(SpriteRendererComponent.class).orderingInLayer;
//                    int tempSwap = currentOrderInLayer;
//
//                    gameObject.getComponent(SpriteRendererComponent.class).orderingInLayer = otherOrder;
//                    objectToSwap.getComponent(SpriteRendererComponent.class).orderingInLayer = tempSwap;
//
//                    Notifications.fireEvent(Notifications.obtainEvent(ComponentUpdated.class).set(gameObject.getComponent(SpriteRendererComponent.class), false));
//                    Notifications.fireEvent(Notifications.obtainEvent(ComponentUpdated.class).set(objectToSwap.getComponent(SpriteRendererComponent.class), false));
//                } else {
//                    //Nothing found, so we can just set it
//                    component.orderingInLayer = MathUtils.clamp(currentOrderInLayer + direction, 0, list.size);
//                    Notifications.fireEvent(Notifications.obtainEvent(ComponentUpdated.class).set(gameObject.getComponent(SpriteRendererComponent.class), false));
//                }
//
//
//            }

        }
    }

    @Override
    public int getPriority () {
        return 1;
    }
}
