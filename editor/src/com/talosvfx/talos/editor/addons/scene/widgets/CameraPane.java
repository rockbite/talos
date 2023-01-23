package com.talosvfx.talos.editor.addons.scene.widgets;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.GameObjectContainer;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.Observer;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.runtime.scene.components.CameraComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CameraPane extends Actor implements Observer {

    private static final Logger logger = LoggerFactory.getLogger(CameraPane.class);
    private final Label title;
    private final Image bg;
    private CameraPreview cameraPreview;

    private Vector2 previewSize = new Vector2();

    private GameObjectContainer gameObjectContainer;
    private GameObject cameraObject;

    public CameraPane () {
        title = new Label("", SharedResources.skin);
        bg = new Image(SharedResources.skin.getDrawable("window"));

        cameraPreview = new CameraPreview();

        Notifications.registerObserver(this);
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {

        bg.setPosition(getX(), getY());
        bg.setSize(getWidth(), getHeight());
        bg.draw(batch, parentAlpha);

        title.setPosition(bg.getX()+15, bg.getY() + getHeight() - title.getHeight()-7);
        title.draw(batch, parentAlpha);

        cameraPreview.setPosition(getX() + 10, getY() + 19);
        cameraPreview.setSize(previewSize.x, previewSize.y);
        cameraPreview.draw(batch, parentAlpha);
    }

    @Override
    public void act (float delta) {
        super.act(delta);

        if (cameraObject!= null) {
            setFrom(this.gameObjectContainer, cameraObject);
        }

        setSize(previewSize.x + 20, previewSize.y  + 49);

        title.setWidth(getWidth() - 40);
        title.setEllipsis(true);

        setPosition(getParent().getWidth() - getWidth(), 0);
        cameraPreview.act(delta);
    }

    public void setFrom (GameObjectContainer gameObjectContainer, GameObject cameraObject) {
        this.gameObjectContainer = gameObjectContainer;
        this.cameraObject = cameraObject;

        CameraComponent component = cameraObject.getComponent(CameraComponent.class);
        Vector2 size = component.size; // viewport size
        float aspect = size.x / size.y;
        float width = 200;
        float height = 200;
        if(aspect > 1f) {
            height = height / aspect;
        }
        if(aspect < 1f) {
            width = width * aspect;
        }
        cameraPreview.setCamera(gameObjectContainer, cameraObject);
        cameraPreview.setViewport(size.x, size.y, width, height);
        previewSize.set(width, height);

        title.setText(cameraObject.getName());

    }
}
