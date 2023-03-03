package com.talosvfx.talos.editor.addons.scene.widgets.gizmos;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.talosvfx.talos.editor.addons.scene.SceneUtils;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;
import com.talosvfx.talos.runtime.scene.components.RendererComponent;
import com.talosvfx.talos.runtime.scene.components.SpriteRendererComponent;
import com.talosvfx.talos.runtime.scene.components.TransformComponent;

public class TransformGizmo extends Gizmo {

    private Vector2 prevTouch = new Vector2();
    private Vector2 vec1 = new Vector2();
    private boolean wasDragged = false;
    private EightPointGizmo spriteTransformGizmo;
    private BoundingBox selectionBounds = new BoundingBox();
    private boolean haveBounds = false;

    private final Vector2 centerPoint = new Vector2();
    private final float pointHitError = 25;

    @Override
    public void draw (Batch batch, float parentAlpha) {
        if(gameObject.hasComponent(TransformComponent.class)) {
            TransformComponent transform = gameObject.getComponent(TransformComponent.class);
            transform.localToWorld(gameObject, tmp.set(0, 0));

            // drawing position point
            if(selected) {
                drawPoint(batch, SharedResources.skin.getRegion("ic-target"), tmp, Color.ORANGE, 30);
                drawBoundedBoxIfNeed(batch);
            }
        }
    }

    private void updateBounds(GameObject object){
        TransformComponent transformComponent = object.getComponent(TransformComponent.class);
        if (object.hasComponent(SpriteRendererComponent.class)) {
            SpriteRendererComponent spriteRendererComponent = object.getComponent(SpriteRendererComponent.class);
            float x = transformComponent.worldPosition.x;
            float y = transformComponent.worldPosition.y;

            Vector2 spriteSize = spriteRendererComponent.size;
            float spriteHeight = spriteSize.y * transformComponent.worldScale.x;
            float spriteWidth = spriteSize.x * transformComponent.worldScale.y;
            float halfWidth = spriteWidth / 2;
            float halfHeight = spriteHeight / 2;

            selectionBounds.ext(x - halfWidth, y - halfHeight, 0);
            selectionBounds.ext(x - halfWidth, y + halfHeight, 0);
            selectionBounds.ext(x + halfWidth, y - halfHeight, 0);
            selectionBounds.ext(x + halfWidth, y + halfHeight, 0);
            haveBounds = true;
        }

        if(object.getGameObjects() !=null) {
            for (GameObject childObject : object.getGameObjects()) {
                updateBounds(childObject);
            }
        }
    }

    private void drawBoundedBoxIfNeed(Batch batch){
        if(gameObject.hasComponentType(RendererComponent.class)){
            return;
        }
        haveBounds = false;
        TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);
        Vector3 minMaxDefault = new Vector3(transformComponent.worldPosition.x, transformComponent.worldPosition.y, 0);
        selectionBounds.set(minMaxDefault, minMaxDefault);
        updateBounds(gameObject);

        if(!haveBounds){
            return;
        }

        float centerX = selectionBounds.getCenterX();
        float centerY = selectionBounds.getCenterY();
        float boundsWidth = selectionBounds.getWidth();
        float boundsHeight = selectionBounds.getHeight();

        drawLine(batch, centerX - boundsWidth/2, centerY - boundsHeight/2, centerX - boundsWidth/2, centerY + boundsHeight/2, ColorLibrary.ORANGE);
        drawLine(batch, centerX + boundsWidth/2, centerY - boundsHeight/2, centerX + boundsWidth/2, centerY + boundsHeight/2, ColorLibrary.ORANGE);

        drawLine(batch, centerX - boundsWidth/2, centerY + boundsHeight/2, centerX + boundsWidth/2, centerY + boundsHeight/2, ColorLibrary.ORANGE);
        drawLine(batch, centerX - boundsWidth/2, centerY - boundsHeight/2, centerX + boundsWidth/2, centerY - boundsHeight/2, ColorLibrary.ORANGE);
    }

    private void drawPoint(Batch batch, TextureRegion region, Vector2 pos, Color color, int size) {
        float finalSize = size * worldPerPixel;
        batch.setColor(color);

        batch.draw(region, pos.x - finalSize / 2f, pos.y - finalSize / 2f, finalSize, finalSize);

        batch.setColor(Color.WHITE);
    }

    private float[] verts = new float[2 * 4];

    @Override
    void getHitBox (Polygon boundingPolygon) {
        if (spriteTransformGizmo != null) {
            //Lets get the size from smart transform and pass it as the rect
            spriteTransformGizmo.getBounds(boundingPolygon);
            return;
        }

        float size = 60 * worldPerPixel;
        boundingPolygon.setPosition(getX(), getY());

        verts[0] = -size/2f;
        verts[1] = -size/2f;

        verts[2] = -size/2f;
        verts[3] = size/2f;

        verts[4] = size/2f;
        verts[5] = size/2f;

        verts[6] = size/2f;
        verts[7] = -size/2f;

        boundingPolygon.setVertices(verts);
    }

    @Override
    public void touchDown (float x, float y, int button) {
        wasDragged = false;
        prevTouch.set(x, y);
        deltaXCache = x;
        deltaYCache = y;

        grabbedFromCenter = isPointHit(centerPoint, x, y);
    }

    private float deltaXCache;
    private float deltaYCache;

    private boolean isPointHit (Vector2 point, float x, float y) {
        float dst = point.dst(x, y) / worldPerPixel;
        // check distance in some error range
        return dst < pointHitError;
    }

    @Override
    protected void positionChanged() {
        super.positionChanged();
        // update central point when position changed
        centerPoint.set(getX(), getY());
    }

    private boolean grabbedFromCenter;


    @Override
    public void touchDragged (float x, float y) {
        // when transform is locked move only when is dragged from center point
        // NOTE: when fast dragged the mouse position can move faster than the transformation, hence we discard checks if the gizmo was already dragged
        if (!grabbedFromCenter && gameObject.isEditorTransformLocked())
            return;

        tmp.set(x, y).sub(prevTouch);
        // render position
        TransformComponent transform = gameObject.getComponent(TransformComponent.class);
        vec1.set(0, 0);
        transform.localToWorld(gameObject.parent, vec1);
        vec1.add(tmp); // change diff
        transform.worldToLocal(gameObject.parent, vec1);
        //vec1 is diff
        transform.position.add(vec1);

        prevTouch.set(x, y);

        wasDragged = true;

        SceneUtils.componentUpdated(gameObjectContainer, gameObject, transform, true);
    }

    @Override
    public void touchUp (float x, float y) {
        boolean shouldUpdate = (!MathUtils.isEqual(deltaXCache, x) || !MathUtils.isEqual(deltaYCache, y));

        if(wasDragged && shouldUpdate) {
            TransformComponent transform = gameObject.getComponent(TransformComponent.class);
            SceneUtils.componentUpdated(gameObjectContainer, gameObject, transform);
        }
    }

    public Vector2 getWorldPos() {
        TransformComponent transform = gameObject.getComponent(TransformComponent.class);
        transform.localToWorld(gameObject, tmp.set(0, 0));

        return tmp;
    }

    @Override
    public boolean isMultiSelect () {
        return true;
    }

    public void linkToSmart (EightPointGizmo spriteTransformGizmo) {
        this.spriteTransformGizmo = spriteTransformGizmo;
    }

    @Override
    public int getPriority () {
        return 1;
    }
}
