package com.talosvfx.talos.editor.addons.scene.widgets;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Pools;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.CameraComponent;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.Observer;
import com.talosvfx.talos.editor.project2.SharedResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CameraPane extends Actor implements Observer {

    private static final Logger logger = LoggerFactory.getLogger(CameraPane.class);
    private final Label title;
    private final Image bg;
    private CameraPreview cameraPreview;

    private Vector2 previewSize = new Vector2();
    private GameObject cameraObject;

    public CameraPane () {
        title = new Label("qaqov tolma", SharedResources.skin);
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
//        if(TalosMain.Instance().Project() instanceof SceneEditorProject) {
//            setVisible(true);
//        } else {
//            setVisible(false);
//            return;
//        }
        super.act(delta);

        if(cameraObject!= null) {
            setFrom(cameraObject);
        }

        setSize(previewSize.x + 20, previewSize.y  + 49);

        title.setWidth(getWidth() - 40);
        title.setEllipsis(true);

        Vector2 vec = Pools.get(Vector2.class).obtain();

        logger.info("Redo camera pane");
        // position specifically
//        Table workspace = SceneEditorAddon.get().workspaceContainer;
//
//        workspace.localToStageCoordinates(vec.set(workspace.getWidth(), 0));

        setPosition(vec.x - getWidth() + 5, vec.y - 8);

        Pools.get(Vector2.class).free(vec);

        cameraPreview.act(delta);
    }

    public void setFrom(GameObject cameraObject) {
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
        cameraPreview.setCamera(cameraObject);
        cameraPreview.setViewport(size.x, size.y, width, height);
        previewSize.set(width, height);

        title.setText(cameraObject.getName());

        this.cameraObject = cameraObject;
    }
}
