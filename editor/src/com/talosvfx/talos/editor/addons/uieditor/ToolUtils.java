package com.talosvfx.talos.editor.addons.uieditor;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class ToolUtils {

    public static Vector2 tmpVec = new Vector2();

    public static Rectangle rectFromActor(Actor actor, Rectangle rectangle) {
        tmpVec.set(0, 0);
        actor.localToStageCoordinates(tmpVec);
        rectangle.setX(tmpVec.x); rectangle.setY(tmpVec.y);
        tmpVec.set(actor.getWidth(), actor.getHeight());
        actor.localToStageCoordinates(tmpVec);
        rectangle.setWidth(tmpVec.x - rectangle.getX()); rectangle.setHeight(tmpVec.y - rectangle.getY());

        return rectangle;
    }

    public static void drawRect(ShapeRenderer shapeRenderer, Rectangle rect, float thickness) {
        shapeRenderer.rectLine(rect.getX(), rect.getY(), rect.getX(), rect.getY() + rect.getHeight(), thickness);
        shapeRenderer.rectLine(rect.getX(), rect.getY() + rect.getHeight(), rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight(), thickness);
        shapeRenderer.rectLine(rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight(), rect.getX() + rect.getWidth(), rect.getY(), thickness);
        shapeRenderer.rectLine(rect.getX() +rect.getWidth(), rect.getY(), rect.getX(), rect.getY(), thickness);

    }
}
