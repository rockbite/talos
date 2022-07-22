package com.talosvfx.talos.editor.addons.scene.widgets.gizmos;

import com.talosvfx.talos.editor.addons.scene.events.ComponentUpdated;
import com.talosvfx.talos.editor.addons.scene.logic.components.SpriteRendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import com.talosvfx.talos.editor.notifications.Notifications;

public class SpriteTransformGizmo extends SmartTransformGizmo {

    public SpriteTransformGizmo() {
        super();
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
        tmp.set(nextPoints[RT]).sub(nextPoints[LB]).scl(0.5f).add(nextPoints[LB]); // this is midpoint
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
