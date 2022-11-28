package com.talosvfx.talos.editor.addons.scene.widgets.gizmos;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntArray;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.events.ComponentUpdated;
import com.talosvfx.talos.editor.addons.scene.logic.components.SpriteRendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.utils.CursorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpriteTransformGizmo extends SmartTransformGizmo {

    private static final Logger logger = LoggerFactory.getLogger(SpriteTransformGizmo.class);

    private Vector2 tempVec2 = new Vector2();

    public SpriteTransformGizmo() {
        super();
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        if (isSelected()) {
            Vector2 vec = new Vector2(Gdx.input.getX(), Gdx.input.getY());

            viewport.screenToLocalCoordinates(vec);
            vec = viewport.getWorldFromLocal(vec.x, vec.y);

            if (isOnTouchedPoint(vec.x, vec.y)) {
                CursorUtil.setDynamicModeCursor(CursorUtil.CursorType.RESIZE);
            } else if (isOnTouchedRotationArea(vec.x, vec.y)) {
                CursorUtil.setDynamicModeCursor(CursorUtil.CursorType.ROTATE);
            }
        }

    }

    public void getBounds (Rectangle rectangle) {

        TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);
        SpriteRendererComponent spriteRendererComponent = gameObject.getComponent(SpriteRendererComponent.class);

        // patch for negative width and height cases
        float signWidth = Math.signum(spriteRendererComponent.size.x);
        float signHeight = Math.signum(spriteRendererComponent.size.y);

        rectangle.set(
                signWidth * -spriteRendererComponent.size.x / 2f, signHeight * -spriteRendererComponent.size.y / 2f,
                signWidth * spriteRendererComponent.size.x, signHeight * spriteRendererComponent.size.y
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

        int howMany90Rots = MathUtils.floor(transform.worldRotation / 90);
        int howMany180Rots = MathUtils.floor(transform.worldRotation / 180f);
        int sig = (int) Math.pow(-1, howMany90Rots + howMany180Rots);

        spriteRendererComponent.size.set(nextPoints[RB].dst(nextPoints[LB]) * Math.signum(nextPoints[RB].x - nextPoints[LB].x) * sig, nextPoints[LB].dst(nextPoints[LT]) * Math.signum(nextPoints[LT].y - nextPoints[LB].y) * sig);
        spriteRendererComponent.size = lowerPrecision(spriteRendererComponent.size);

        // if aspect ratio is fixed set height by width
        if (spriteRendererComponent.fixAspectRatio) {
            Texture texture = spriteRendererComponent.getGameResource().getResource();

            if (texture != null) {
                final float aspect = texture.getHeight() * 1f / texture.getWidth();
                spriteRendererComponent.size.y = spriteRendererComponent.size.x * aspect;
            }
        }

        if (spriteRendererComponent.fixAspectRatio) {



            if (touchedPoint == RT) {
                tempVec2.set(spriteRendererComponent.size).scl(0.5f).rotateDeg(transform.rotation);

                transform.position.set(nextPoints[LB]).add(tempVec2.x, tempVec2.y);
            } else if (touchedPoint == LT) {
                tempVec2.set(spriteRendererComponent.size).scl(0.5f).rotateDeg(-transform.rotation);

                transform.position.set(nextPoints[RB]).add(-tempVec2.x, tempVec2.y);
            } else if (touchedPoint == LB) {
                tempVec2.set(spriteRendererComponent.size).scl(0.5f).rotateDeg(transform.rotation);

                transform.position.set(nextPoints[RT]).add(-tempVec2.x, -tempVec2.y);
            } else if (touchedPoint == RB) {
                tempVec2.set(spriteRendererComponent.size).scl(0.5f).rotateDeg(-transform.rotation);

                transform.position.set(nextPoints[LT]).add(tempVec2.x, -tempVec2.y);
            }

            transform.position.sub(gameObject.getTransformSettings().offsetX, gameObject.getTransformSettings().offsetY);

        } else {
            tempVec2.set(spriteRendererComponent.size).scl(0.5f).rotateDeg(transform.rotation);
            transform.position.set(nextPoints[LB]).add(tempVec2);
            transform.position.sub(gameObject.getTransformSettings().offsetX, gameObject.getTransformSettings().offsetY);
        }

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
