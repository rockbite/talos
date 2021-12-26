package com.talosvfx.talos.editor.addons.scene.widgets.gizmos;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.IComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;

public abstract class Gizmo extends Actor {

    private IComponent component;
    private GameObject gameObject;

    Vector2 tmp = new Vector2();

    protected float worldPerPixel;

    public void setComponent (IComponent component) {
        this.component = component;
    }

    public void setGameObject(GameObject gameObject) {
        this.gameObject = gameObject;
    }

    public void act(float delta) {
        super.act(delta);
        tmp.set(0, 0);
        getTransformPosition(tmp);
        setPosition(tmp.x, tmp.y);
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
    }

    public Vector2 getTransformPosition(Vector2 pos) {
        if(gameObject.hasComponent(TransformComponent.class)) {
            TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);
            pos.set(transformComponent.position);

            return pos;
        } else {
            pos.set(0, 0);
        }

        return pos;
    }

    public void setWoldWidth (float worldWidth) {
        int screenPixels = Gdx.graphics.getWidth();
        worldPerPixel = worldWidth / screenPixels;
    }
}
