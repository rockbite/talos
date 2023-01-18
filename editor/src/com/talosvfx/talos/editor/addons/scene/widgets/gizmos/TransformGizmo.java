package com.talosvfx.talos.editor.addons.scene.widgets.gizmos;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.talosvfx.talos.editor.addons.scene.SceneUtils;
import com.talosvfx.talos.runtime.scene.GameObject;import com.talosvfx.talos.editor.addons.scene.logic.componentwrappers.RendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.componentwrappers.SpriteRendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.componentwrappers.TransformComponent;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;

public class TransformGizmo extends Gizmo {

    private Vector2 prevTouch = new Vector2();
    private Vector2 vec1 = new Vector2();
    private boolean wasDragged = false;
    private SpriteTransformGizmo spriteTransformGizmo;
    private BoundingBox selectionBounds = new BoundingBox();
    private boolean haveBounds = false;

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

    @Override
    void getHitBox (Rectangle rectangle) {
        if (spriteTransformGizmo != null) {
            //Lets get the size from smart transform and pass it as the rect
            spriteTransformGizmo.getBounds(rectangle);
            return;
        }

        float size = 60 * worldPerPixel;
        rectangle.set(getX() - size / 2f, getY() - size / 2f, size, size);
    }

    @Override
    public void touchDown (float x, float y, int button) {
        wasDragged = false;
        prevTouch.set(x, y);
        deltaXCache = x;
        deltaYCache = y;
    }

    private float deltaXCache;
    private float deltaYCache;

    @Override
    public void touchDragged (float x, float y) {

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

    public void linkToSmart (SpriteTransformGizmo spriteTransformGizmo) {
        this.spriteTransformGizmo = spriteTransformGizmo;
    }
}
