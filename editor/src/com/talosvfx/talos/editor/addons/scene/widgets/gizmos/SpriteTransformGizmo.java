package com.talosvfx.talos.editor.addons.scene.widgets.gizmos;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.events.ComponentUpdated;
import com.talosvfx.talos.editor.addons.scene.logic.components.SpriteRendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.utils.CursorUtil;

public class SpriteTransformGizmo extends SmartTransformGizmo {

    public SpriteTransformGizmo() {
        super();
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        Vector2 vec = new Vector2(Gdx.input.getX(), Gdx.input.getY());

        SceneEditorWorkspace.getInstance().screenToLocalCoordinates(vec);
        vec = SceneEditorWorkspace.getInstance().getWorldFromLocal(vec.x, vec.y);

        if (isOnTouchedPoint(vec.x, vec.y)) {
            CursorUtil.setDynamicModeCursor(CursorUtil.CursorType.RESIZE);
        } else if (isOnTouchedRotationArea(vec.x, vec.y)) {
            CursorUtil.setDynamicModeCursor(CursorUtil.CursorType.ROTATE);
        }

    }

    public void getBounds (Rectangle rectangle) {

        TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);
        SpriteRendererComponent spriteRendererComponent = gameObject.getComponent(SpriteRendererComponent.class);

        rectangle.set(
            -spriteRendererComponent.size.x/2f, -spriteRendererComponent.size.y/2f,
            spriteRendererComponent.size.x, spriteRendererComponent.size.y
            );

        rectangle.x += transformComponent.worldPosition.x;
        rectangle.y += transformComponent.worldPosition.y;
    }

    @Override
    protected void updatePointsFromComponent () {
        getWorldLocAround(tmp, 0, 0); // this is center position of camera

        TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);
        SpriteRendererComponent spriteRendererComponent = gameObject.getComponent(SpriteRendererComponent.class);

        points[LB].set(tmp.x - spriteRendererComponent.size.x/2f, tmp.y - spriteRendererComponent.size.y/2f);
        points[LT].set(tmp.x - spriteRendererComponent.size.x/2f, tmp.y + spriteRendererComponent.size.y/2f);
        points[RT].set(tmp.x + spriteRendererComponent.size.x/2f, tmp.y + spriteRendererComponent.size.y/2f);
        points[RB].set(tmp.x + spriteRendererComponent.size.x/2f, tmp.y - spriteRendererComponent.size.y/2f);

        points[LB].rotateAroundDeg(tmp, transformComponent.rotation);
        points[LT].rotateAroundDeg(tmp, transformComponent.rotation);
        points[RT].rotateAroundDeg(tmp, transformComponent.rotation);
        points[RB].rotateAroundDeg(tmp, transformComponent.rotation);

        tmp.set(points[RT]).sub(points[LB]).scl(0.5f).add(points[LB]); // midpoint
        updateRotationAreas(tmp.x, tmp.y);
    }

    @Override
    protected void transformOldToNew () {
        TransformComponent transform = gameObject.getComponent(TransformComponent.class);
        SpriteRendererComponent spriteRendererComponent = gameObject.getComponent(SpriteRendererComponent.class);

        // bring old next points to local space
        for(int i = 0; i < 4; i++) {
            TransformComponent.worldToLocal(gameObject.parent, nextPoints[i]);
        }

        spriteRendererComponent.size.set(nextPoints[RB].dst(nextPoints[LB]), nextPoints[LB].dst(nextPoints[LT]));
        spriteRendererComponent.size = lowerPrecision(spriteRendererComponent.size);

        // if aspect ratio is fixed set height by width
        if (spriteRendererComponent.fixAspectRatio) {
            Texture texture = spriteRendererComponent.getGameResource().getResource();

            final float aspect = texture.getHeight() * 1f / texture.getWidth();
            spriteRendererComponent.size.y = spriteRendererComponent.size.x * aspect;
        }


        if (touchedPoint == RT) {
            transform.position.set(nextPoints[LB]).add(spriteRendererComponent.size.x / 2f, spriteRendererComponent.size.y / 2f);
        } else if (touchedPoint == LT) {
            transform.position.set(nextPoints[RB]).add(-spriteRendererComponent.size.x / 2f, spriteRendererComponent.size.y / 2f);
        } else if (touchedPoint == LB) {
            transform.position.set(nextPoints[RT]).add(-spriteRendererComponent.size.x / 2f, -spriteRendererComponent.size.y / 2f);
        } else if (touchedPoint == RB) {
            transform.position.set(nextPoints[LT]).add(spriteRendererComponent.size.x / 2f, -spriteRendererComponent.size.y / 2f);
        }

//        transform.position.set(nextPoints[LB]).add(spriteRendererComponent.size.x / 2f, spriteRendererComponent.size.y / 2f);

        transform.position = lowerPrecision(transform.position);
    }

    @Override
    protected void reportResizeUpdated (boolean isRapid) {
        TransformComponent transform = gameObject.getComponent(TransformComponent.class);
        Notifications.fireEvent(Notifications.obtainEvent(ComponentUpdated.class).set(transform, isRapid));

        SpriteRendererComponent spriteRendererComponent = gameObject.getComponent(SpriteRendererComponent.class);
        Notifications.fireEvent(Notifications.obtainEvent(ComponentUpdated.class).set(spriteRendererComponent, isRapid));
    }


}
