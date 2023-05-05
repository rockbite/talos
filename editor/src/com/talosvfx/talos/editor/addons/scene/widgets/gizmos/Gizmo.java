package com.talosvfx.talos.editor.addons.scene.widgets.gizmos;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Pool;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.ViewportWidget;
import com.talosvfx.talos.runtime.scene.GameObjectContainer;
import com.talosvfx.talos.runtime.scene.components.AComponent;
import com.talosvfx.talos.runtime.scene.components.TransformComponent;
import com.talosvfx.talos.runtime.scene.utils.TransformSettings;

import java.util.function.Supplier;

public abstract class Gizmo extends Actor implements Pool.Poolable {

    protected ViewportWidget viewport;

    public void setViewport (ViewportWidget viewport) {
        this.viewport = viewport;
    }



    protected AComponent component;
    protected GameObject gameObject;
    protected GameObjectContainer gameObjectContainer;

    Vector2 tmp = new Vector2();

    protected float worldPerPixel = 1f;

    protected Polygon hitBox = new Polygon();

    protected boolean selected = false;

    private Image circle;

    public Gizmo() {
        circle = new Image(SharedResources.skin.getDrawable("vfx-green"));
    }

    public void setGameObject(GameObjectContainer gameObjectContainer, GameObject gameObject) {
        this.gameObjectContainer = gameObjectContainer;
        this.gameObject = gameObject;
    }

    public void act(float delta) {
        super.act(delta);
        updateFromGameObject();
    }

    protected void updateFromGameObject () {
        if(gameObject.hasComponent(TransformComponent.class)) {
            TransformComponent transform = gameObject.getComponent(TransformComponent.class);
            tmp.set(0, 0);
            transform.localToWorld(gameObject, tmp);
            setPosition(tmp.x, tmp.y);
        }
    }


    protected void drawLine(Batch batch, Vector2 from, Vector2 to, Color color) {
        drawLine(batch, from.x, from.y, to.x, to.y, color);
    }

    protected void drawLine(Batch batch, float x1, float y1, float x2, float y2, Color color) {
        TextureRegion white = SharedResources.skin.getRegion("white");
        tmp.set(x2, y2).sub(x1, y1);
        float thickness = worldPerPixel * 3f;
        float length = tmp.len();
        float rotation = tmp.angleDeg();
        tmp.scl(0.5f).add(x1, y1); // center points
        Color prev = batch.getColor();
        batch.setColor(color);
        batch.draw(white, tmp.x - 0.5f * length, tmp.y - 0.5f * thickness, length/2f, thickness/2f, length, thickness, 1f, 1f, rotation);
        batch.setColor(prev);
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
    }

    public boolean hit (float x, float y) {
        if(gameObject == null) return false;
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

    public void setSizeForUIElements (float totalScreenSpaceParentSize, float totalWorldWidth) {
        worldPerPixel = getViewPortScale() * totalWorldWidth / totalScreenSpaceParentSize;
    }

    public float getViewPortScale(){
        final Supplier<Camera> currentCameraSupplier = viewport.getViewportViewSettings().getCurrentCameraSupplier();
        return currentCameraSupplier.get().viewportWidth / 10;
    }

    void getHitBox(Polygon boudningPolygon) {

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

    public void keyUp(InputEvent event, int keycode) {

    }

    public void snapIfRequired () {
        boolean hasTransform = gameObject.hasComponent(TransformComponent.class);

        if (hasTransform) {
            if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                TransformSettings transformSettings = gameObject.getTransformSettings();
                TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);
                snapToGrid(transformSettings.gridSizeX, transformSettings.gridSizeY, transformComponent.position, new Vector2());
            }
        }

    }
    protected void snapToGrid (float gridSizeX, float gridSizeY, Vector2 position, Vector2 offset) {
        float positionX = MathUtils.round(position.x/gridSizeX) * gridSizeX;
        float positionY = MathUtils.round(position.y/gridSizeY) * gridSizeY;
        position.x = positionX;
        position.y = positionY;
    }

    @Override
    public void reset () {

    }

    public boolean isMultiSelect() {
        return false;
    }

    public void notifyRemove () {

    }

    protected void drawCircle(Vector2 pos, Batch batch) {
        float size = 20 * worldPerPixel; // pixel
        circle.setSize(size, size);
        circle.setPosition(pos.x - size/2f, pos.y-size/2f);
        circle.draw(batch, 1f);
    }

    public boolean catchesShift() {
        return false;
    }

    public void mouseMoved(float x, float y) {

    }

    public int getPriority () {
        return 0;
    }

    public boolean isControllingGameObject (GameObject gameObject) {
        return this.gameObject == gameObject;
    }
}
