package com.talosvfx.talos.editor.addons.uieditor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.talosvfx.talos.editor.addons.uieditor.runtime.UICanvas;
import com.talosvfx.talos.editor.addons.uieditor.runtime.anchors.Anchor;

public class BoxTransform implements Pool.Poolable {

    private UIWorkspace workspace;

    private Rectangle rectangle = new Rectangle();
    private Array<Vector2> points = new Array();
    private Actor actor;

    private boolean centerTransform = true;

    private Color toolBoxColor = new Color(0x009ce5ff);
    private Color highlightColor = new Color(Color.WHITE);

    private static float POINT_SIZE = 8f;
    private static float THICKNESS = 2f;

    private static int LEFT_BOTTOM = 0;
    private static int LEFT_TOP = 1;
    private static int RIGHT_TOP = 2;
    private static int RIGHT_BOTTOM = 3;

    private int selectedPoint = -1;
    private Vector2 touchOffset = new Vector2();
    private Vector2 tmp = new Vector2();
    private Rectangle tmpRect = new Rectangle();
    private Circle circle = new Circle();

    private Vector2 hoverPosition = new Vector2();

    private boolean moving = false;

    public BoxTransform() {
        for(int i = 0; i < 4; i++) {
            points.add(new Vector2());
        }
    }

    public boolean touchDown (float x, float y) {
        selectedPoint = -1;

        for(int i = 0; i < points.size; i++) {
            Vector2 point = points.get(i);
            float pointSize = getPointSize() * 4f; // bigger hit-box

            if(x >= point.x - pointSize/2f && x <= point.x + pointSize/2f && y >= point.y - pointSize/2f && y <= point.y + pointSize/2f) {
                selectedPoint = i;
                touchOffset.set(x - point.x, y - point.y);
                break;
            }
        }

        if(selectedPoint == -1) {
            circle = getDragCircle();
            if(circle.contains(x, y)) {
                moving = true;
                touchOffset.set(x - circle.x, y - circle.y);
            }
        }

        return selectedPoint >= 0;
    }

    public void touchDragged (float x, float y) {
        if(selectedPoint >= 0) {
            Vector2 selected = points.get(selectedPoint);
            selected.set(x - touchOffset.x, y - touchOffset.y);

            if(Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                centerTransform = false;
            } else {
                centerTransform = true;
            }

            if(!centerTransform) {
                if(selectedPoint == LEFT_BOTTOM) {
                    points.get(LEFT_TOP).x = selected.x;
                    points.get(RIGHT_BOTTOM).y = selected.y;
                }
                if(selectedPoint == LEFT_TOP) {
                    points.get(RIGHT_TOP).y = selected.y;
                    points.get(LEFT_BOTTOM).x = selected.x;
                }
                if(selectedPoint == RIGHT_TOP) {
                    points.get(LEFT_TOP).y = selected.y;
                    points.get(RIGHT_BOTTOM).x = selected.x;
                }
                if(selectedPoint == RIGHT_BOTTOM) {
                    points.get(RIGHT_TOP).x = selected.x;
                    points.get(LEFT_BOTTOM).y = selected.y;
                }
            } else {
                float scaleX = Math.abs(((rectangle.x + rectangle.width/2f) - selected.x))/(rectangle.width/2f);
                float scaleY = Math.abs(((rectangle.y + rectangle.height/2f) - selected.y))/(rectangle.height/2f);

                for (int i = 0; i < points.size; i++) {
                    if(points.get(i) == selected) continue;
                    tmp.set(points.get(i).x - (rectangle.x + rectangle.width/2f), points.get(i).y - (rectangle.y + rectangle.height/2f));
                    tmp.scl(scaleX, scaleY);
                    tmp.add((rectangle.x + rectangle.width/2f), (rectangle.y + rectangle.height/2f));
                    points.get(i).set(tmp);
                }
            }

            updateActorFromPoints();
        }

        if (moving && !(actor instanceof UICanvas)) {
            rectangle.getCenter(tmp);
            tmp.sub(x - touchOffset.x, y - touchOffset.y);
            tmpRect.set(rectangle);
            tmpRect.x -= tmp.x;
            tmpRect.y -= tmp.y;
            points.get(LEFT_BOTTOM).set(tmpRect.x, tmpRect.y);
            points.get(LEFT_TOP).set(tmpRect.x, tmpRect.y + tmpRect.height);
            points.get(RIGHT_TOP).set(tmpRect.x + tmpRect.width, tmpRect.y + tmpRect.height);
            points.get(RIGHT_BOTTOM).set(tmpRect.x + tmpRect.width, tmpRect.y);
            updateActorFromPoints();
        }
    }

    private void updateActorFromPoints () {
        Anchor anchor = workspace.getCanvas().getAnchor(actor);

        tmp.set(points.get(LEFT_BOTTOM).x, points.get(LEFT_BOTTOM).y);
        if(actor.hasParent()) {
            actor.getParent().stageToLocalCoordinates(tmp);
        }

        if (anchor != null && anchor.isAnchored()) {
            anchor.updateAnchorFromSize(points.get(RIGHT_TOP).x - points.get(LEFT_BOTTOM).x, points.get(RIGHT_TOP).y - points.get(LEFT_BOTTOM).y);
            anchor.setOffsetFromPosition(points.get(RIGHT_TOP).x - points.get(LEFT_BOTTOM).x, points.get(RIGHT_TOP).y - points.get(LEFT_BOTTOM).y, tmp.x, tmp.y);
            workspace.getCanvas().invalidateHierarchy(actor);
        } else {
            actor.setPosition(tmp.x, tmp.y);
            actor.setSize(points.get(RIGHT_TOP).x - points.get(LEFT_BOTTOM).x, points.get(RIGHT_TOP).y - points.get(LEFT_BOTTOM).y);
        }

        setActor(actor);
    }

    public void mouseMoved (float x, float y) {
        hoverPosition.set(x, y);
    }

    public void touchUp (float x, float y) {
        selectedPoint = -1;
        moving = false;
    }

    @Override
    public void reset () {
        actor = null;
        selectedPoint = -1;
    }

    public void setWorkspace (UIWorkspace workspace) {
        this.workspace = workspace;
    }

    public Actor getActor () {
        return actor;
    }

    public interface BoxTransformListener {
        void transform(float x, float y, float width, float height);
    }

     public void setActor(Actor actor) {
        this.actor = actor;

         ToolUtils.rectFromActor(this.actor, rectangle);

         points.get(LEFT_BOTTOM).set(rectangle.x, rectangle.y);
         points.get(LEFT_TOP).set(rectangle.x, rectangle.y + rectangle.height);
         points.get(RIGHT_TOP).set(rectangle.x + rectangle.width, rectangle.y + rectangle.height);
         points.get(RIGHT_BOTTOM).set(rectangle.x + rectangle.width, rectangle.y);
     }

    public void draw(ShapeRenderer shapeRenderer) {
        if(actor == null) return;

        shapeRenderer.setColor(toolBoxColor);

        float zoom = workspace.getCameraZoom() / (workspace.getWorldWidth()/workspace.getCamera().viewportWidth);
        float thickness = zoom * THICKNESS;

        ToolUtils.drawRect(shapeRenderer, rectangle, thickness);

        // draw hover highlight if needed
        drawHighlight(shapeRenderer);

        shapeRenderer.setColor(toolBoxColor);
        for(int i = 0; i < points.size; i++) {
            drawPointBox(shapeRenderer, points.get(i).x, points.get(i).y);
        }
    }

    private Circle getDragCircle() {
        rectangle.getCenter(tmp);
        circle.setPosition(tmp);
        float radius = 30;
        float minSize = Math.min(rectangle.getWidth(), rectangle.getHeight())/2f;
        if(radius > minSize) radius = minSize;
        circle.setRadius(radius);

        return circle;
    }

    private void drawHighlight (ShapeRenderer shapeRenderer) {
        if(rectangle.contains(hoverPosition) && !(actor instanceof UICanvas)) {
            circle = getDragCircle();

            highlightColor.a = 0.1f;
            if(circle.contains(hoverPosition)) {
                highlightColor.a = 0.3f;
            }

            shapeRenderer.setColor(highlightColor);
            shapeRenderer.circle(circle.x, circle.y, circle.radius, 20);
        }
    }

    private void drawPointBox(ShapeRenderer shapeRenderer, float x, float y) {
        float pointSizeAdjusted = getPointSize();
        float pointSizeAdjustedHalf = pointSizeAdjusted / 2f;

        shapeRenderer.rect(x - pointSizeAdjustedHalf, y - pointSizeAdjustedHalf, pointSizeAdjusted, pointSizeAdjusted);
    }

    private float getPointSize() {
        float zoom = workspace.getCameraZoom() / (workspace.getWorldWidth()/workspace.getCamera().viewportWidth);
        float pointSizeAdjusted = zoom * POINT_SIZE;

        return pointSizeAdjusted;
    }
}
