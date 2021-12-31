package com.talosvfx.talos.editor.addons.scene.widgets.gizmos;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.events.ComponentUpdated;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import com.talosvfx.talos.editor.notifications.Notifications;

public class TransformGizmo extends Gizmo {

    private Vector2 prevTouch = new Vector2();
    private Vector2 vec1 = new Vector2();
    private boolean wasDragged = false;

    @Override
    public void draw (Batch batch, float parentAlpha) {
        if(gameObject.hasComponent(TransformComponent.class)) {
            TransformComponent transform = gameObject.getComponent(TransformComponent.class);
            transform.localToWorld(gameObject, tmp.set(0, 0));

            // drawing position point
            if(selected) {
                drawPoint(batch, TalosMain.Instance().getSkin().getRegion("ic-target"), tmp, Color.ORANGE, 30);
            } else {
                drawPoint(batch, TalosMain.Instance().getSkin().getRegion("ic-target"), tmp, Color.WHITE, 30);
            }
        }



    }

    private void drawPoint(Batch batch, TextureRegion region, Vector2 pos, Color color, int size) {
        float finalSize = size * worldPerPixel;
        batch.setColor(color);

        batch.draw(region, pos.x - finalSize / 2f, pos.y - finalSize / 2f, finalSize, finalSize);

        batch.setColor(Color.WHITE);
    }

    @Override
    void getHitBox (Rectangle rectangle) {
        float size = 30 * worldPerPixel;
        rectangle.set(getX() - size / 2f, getY() - size / 2f, size, size);
    }

    @Override
    public void touchDown (float x, float y, int button) {
        wasDragged = false;
        prevTouch.set(x, y);
    }

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



        Notifications.fireEvent(Notifications.obtainEvent(ComponentUpdated.class).set(transform, true));
    }

    @Override
    public void touchUp (float x, float y) {
        if(wasDragged) {
            TransformComponent transform = gameObject.getComponent(TransformComponent.class);
            Notifications.fireEvent(Notifications.obtainEvent(ComponentUpdated.class).set(transform));
        }
    }
}
