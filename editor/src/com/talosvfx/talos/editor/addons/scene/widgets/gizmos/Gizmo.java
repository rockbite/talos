package com.talosvfx.talos.editor.addons.scene.widgets.gizmos;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.Pool;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.IComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;

public abstract class Gizmo extends Actor implements Pool.Poolable {

    protected IComponent component;
    protected GameObject gameObject;

    Vector2 tmp = new Vector2();

    protected float worldPerPixel;

    protected Rectangle hitBox = new Rectangle();

    protected boolean selected = false;

    public void setGameObject(GameObject gameObject) {
        this.gameObject = gameObject;
    }

    public void act(float delta) {
        super.act(delta);

        if(gameObject.hasComponent(TransformComponent.class)) {
            TransformComponent transform = gameObject.getComponent(TransformComponent.class);
            tmp.set(0, 0);
            transform.localToWorld(gameObject, tmp);
            setPosition(tmp.x, tmp.y);
        }
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
    }

    public boolean hit (float x, float y) {
        getHitBox(hitBox);

        if (hitBox.contains(x, y)) {
            return true;
        }

        return false;
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

    void getHitBox(Rectangle rectangle) {

    }

    public GameObject getGameObject() {
        return gameObject;
    }

    public void touchDown (float x, float y, int button) {

    }

    public void touchDragged (float x, float y) {

    }

    public void touchUp (float x, float y) {

    }

    public void setSelected (boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected () {
        return selected;
    }

    public void keyDown (InputEvent event, int keycode) {

    }

    @Override
    public void reset () {

    }

    public boolean isMultiSelect() {
        return false;
    }
}
