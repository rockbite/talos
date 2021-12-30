package com.talosvfx.talos.editor.addons.scene.widgets.gizmos;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.SceneEditorAddon;
import com.talosvfx.talos.editor.addons.scene.events.ComponentUpdated;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.SpriteRendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import com.talosvfx.talos.editor.notifications.Notifications;

public class SmartTransformGizmo extends Gizmo<SpriteRendererComponent> {

    private final Image rect;
    private final Image circle;
    private final NinePatch rectPatch;
    private float prevScale = 1f;

    private Vector2[] points = new Vector2[4];
    private Vector2[] rotationAreas = new Vector2[4];

    private static final int LB = 0;
    private static final int LT = 1;
    private static final int RT = 2;
    private static final int RB = 3;
    private int touchedPoint = -1;
    private int touchedRA = -1;

    private Vector2 touchedDownLocation = new Vector2();
    private Vector2 prevDragLocation = new Vector2();


    private Vector2 tmp = new Vector2();
    private Vector2 tmp2 = new Vector2();
    private Vector2 tmp3 = new Vector2();
    private Vector2 tmp4 = new Vector2();

    private Vector2[] prevPoints = new Vector2[4];
    private Vector2[] nextPoints = new Vector2[4];
    private float prevRotation;
    private float nextRotation;

    public SmartTransformGizmo() {
        NinePatchDrawable rectDrawable = (NinePatchDrawable) TalosMain.Instance().getSkin().getDrawable("border-dark-orange");
        rect = new Image(rectDrawable);
        rectPatch = rectDrawable.getPatch();

        circle = new Image(TalosMain.Instance().getSkin().getDrawable("vfx-green"));

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
            TransformComponent transform = gameObject.getComponent(TransformComponent.class);

            for (int i = 0; i < 4; i++) {
                drawCircle(points[i], batch);
            }
        }

    }

    private Vector2 getWorldLocAround(Vector2 point, float x, float y) {
        point.set(x, y);
        TransformComponent.localToWorld(gameObject, point);

        return point;
    }

    private void drawCircle(Vector2 pos, Batch batch) {
        float size = 20 * worldPerPixel; // pixel
        circle.setSize(size, size);
        circle.setPosition(pos.x - size/2f, pos.y-size/2f);
        circle.draw(batch, 1f);
    }

    @Override
    public boolean hit (float x, float y) {
        if(!selected) return false;

        int touchedPoint = getTouchedPoint(x, y);
        int touchedRA = getTouchedRotationArea(x, y);
        if(touchedPoint >= 0) {
            return true;
        }
        if(touchedRA >= 0) {
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
            TransformComponent transform = gameObject.getComponent(TransformComponent.class);
            Notifications.fireEvent(Notifications.obtainEvent(ComponentUpdated.class).set(transform, false));
        } else if (touchedRA >= 0) {
            applyRotationChange(x, y);
            updateRotationAreas(tmp.x, tmp.y);
        }

        prevDragLocation.set(x, y);
    }

    @Override
    public void touchUp (float x, float y) {
        if(touchedPoint >= 0) {
            setNewPointValue(touchedPoint, x, y);
            updatePointsFromComponent();
            TransformComponent transform = gameObject.getComponent(TransformComponent.class);
            Notifications.fireEvent(Notifications.obtainEvent(ComponentUpdated.class).set(transform));
        } else if (touchedRA >= 0) {
            applyRotationChange(x, y);
            updateRotationAreas(tmp.x, tmp.y);
        }
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

    private void updatePointsFromComponent () {
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
    private void updateRotationAreas (float x, float y) {
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

        // tmp2 contains movement diff
        tmp2.set(x, y).sub(points[touchedPoint]);
        // find midpoint
        tmp3.set(points[RT]).sub(points[LB]).scl(0.5f).add(points[LB]);

        for(int i = 0; i < 4; i++) {
            points[i].sub(tmp3);
            points[i].rotateDeg(-prevRotation);
        }
        tmp2.rotateDeg(-prevRotation);
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

    private void transformOldToNew () {
        TransformComponent transform = gameObject.getComponent(TransformComponent.class);

        // bring old next points to local space
        for(int i = 0; i < 4; i++) {
            TransformComponent.worldToLocal(gameObject.parent, nextPoints[i]);
        }

        transform.scale.set(nextPoints[RB].dst(nextPoints[LB]), nextPoints[LB].dst(nextPoints[LT]));
        transform.scale = lowerPrecision(transform.scale);
        tmp.set(nextPoints[RT]).sub(nextPoints[LB]).scl(0.5f).add(nextPoints[LB]); // this is midpoint
        transform.position = lowerPrecision(transform.position);
    }

    private Vector2 lowerPrecision(Vector2 vec) {
        vec.x = lowerPrecision(vec.x);
        vec.y = lowerPrecision(vec.y);

        return vec;
    }

    private float lowerPrecision(float number) {
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
            //TransformComponent.worldToLocal(gameObject.parent, pointArray[i]);
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

    private void moveInLayerOrder (GameObject gameObject, int direction) {
        // direction -1 for down, 1 for up
        if(gameObject.hasComponent(SpriteRendererComponent.class)) {
            SpriteRendererComponent component = gameObject.getComponent(SpriteRendererComponent.class);
            String sortingLayer = component.sortingLayer;

            Array<GameObject> list = SceneEditorAddon.get().workspace.getRootGO().getChildrenByComponent(SpriteRendererComponent.class, new Array<>());
            for(int i = list.size - 1; i >= 0; i--) {
                if(!list.get(i).getComponent(SpriteRendererComponent.class).sortingLayer.equals(sortingLayer)) {
                    list.removeIndex(i);
                }
            }

            // find closest game object if any that is lower to my index, and swap with it
            if(list.size > 1) {
                GameObject closest = list.first();
                if(list.first() == gameObject) {
                    closest = list.get(1);
                }
                int origDst = component.orderingInLayer - closest.getComponent(SpriteRendererComponent.class).orderingInLayer;

                for(GameObject candidate: list) {
                    int dst = component.orderingInLayer - candidate.getComponent(SpriteRendererComponent.class).orderingInLayer;
                    boolean matchesDirection = false;
                    boolean matchesDirectionEquals = false;
                    if(direction == -1 && dst >= 0) matchesDirectionEquals = true;
                    if(direction == 1 && dst <= 0) matchesDirectionEquals = true;
                    if(direction == -1 && origDst > dst) matchesDirection = true;
                    if(direction == 1 && origDst < dst) matchesDirection = true;

                    if(matchesDirectionEquals && candidate != gameObject) {
                        if(matchesDirection) {
                            origDst = dst;
                            closest = candidate;
                        }
                    }
                }

                SpriteRendererComponent closestComponent = closest.getComponent(SpriteRendererComponent.class);
                if(closestComponent.orderingInLayer > component.orderingInLayer && direction == -1) {
                    return;
                }
                if(closestComponent.orderingInLayer < component.orderingInLayer && direction == 1) {
                    return;
                }
                if(closestComponent.orderingInLayer == component.orderingInLayer) {
                    closestComponent.orderingInLayer-=direction;
                } else {
                    // swap
                    int tmp = component.orderingInLayer;
                    component.orderingInLayer = closestComponent.orderingInLayer;
                    closestComponent.orderingInLayer = tmp;

                    Notifications.fireEvent(Notifications.obtainEvent(ComponentUpdated.class).set(component, false));
                }
                Notifications.fireEvent(Notifications.obtainEvent(ComponentUpdated.class).set(closestComponent, false));
            }
        }
    }
}
