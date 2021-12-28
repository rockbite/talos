package com.talosvfx.talos.editor.addons.scene.widgets.gizmos;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.events.ComponentUpdated;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import com.talosvfx.talos.editor.notifications.Notifications;

public class TransformGizmo extends Gizmo<TransformComponent> {

    private static final int SIZE = 30; // pixels

    private Vector2 prevTouch = new Vector2();

    @Override
    public void draw (Batch batch, float parentAlpha) {
        TextureRegion region = TalosMain.Instance().getSkin().getRegion("ic-target");
        float size = SIZE * worldPerPixel;

        batch.draw(region, getX() - size / 2f, getY() - size / 2f, size, size);
    }

    @Override
    void getHitBox (Rectangle rectangle) {
        float size = SIZE * worldPerPixel;
        rectangle.set(getX() - size / 2f, getY() - size / 2f, size, size);
    }

    @Override
    public void touchDown (float x, float y, int button) {
        prevTouch.set(x, y);
    }

    @Override
    public void touchDragged (float x, float y) {
        tmp.set(x, y).sub(prevTouch);

        component.position.add(tmp);

        prevTouch.set(x, y);

        Notifications.fireEvent(Notifications.obtainEvent(ComponentUpdated.class).set(component));
    }

    @Override
    public void touchUp (float x, float y) {

    }
}
