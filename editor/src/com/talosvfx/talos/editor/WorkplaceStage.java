package com.talosvfx.talos.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.utils.CameraController;

public abstract class WorkplaceStage {
    protected Stage stage;

    protected Color bgColor = new Color();

    private CameraController cameraController;

    public WorkplaceStage() {
        stage = new Stage(new ScreenViewport(), new PolygonSpriteBatch());
        cameraController = new CameraController(getCamera());
    }

    public OrthographicCamera getCamera() {
        return (OrthographicCamera) stage.getCamera();
    }

    public abstract void init();

    public Stage getStage() {
        return stage;
    }

    public void resize (int width, int height) {
        stage.getViewport().update(width, height);
    }

    public CameraController getCameraController() {
        return cameraController;
    }

    protected void initListeners() {
        stage.addListener(new InputListener() {

            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
                cameraController.scrolled(amountX, amountY);
                return super.scrolled(event, x, y, amountX, amountY);
            }

            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                TalosMain.Instance().UIStage().getStage().unfocusAll();

                if(TalosMain.Instance().getCameraController() != null) {
                    TalosMain.Instance().getCameraController().touchDown(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY(), pointer, button);
                } else {
                    cameraController.touchDown(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY(), pointer, button);
                }

                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                if(event.isHandled()) return;

                super.touchDragged(event, x, y, pointer);

                if(TalosMain.Instance().getCameraController() != null) {
                    TalosMain.Instance().getCameraController().touchDragged(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY(), pointer);
                } else {
                    cameraController.touchDragged(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY(), pointer);
                }
            }

            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);

                if(TalosMain.Instance().getCameraController() != null) {
                    TalosMain.Instance().getCameraController().touchUp(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY(), pointer, button);
                } else {
                    cameraController.touchUp(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY(), pointer, button);
                }
            }

            @Override
            public boolean keyDown(InputEvent event, int keycode) {

                return super.keyDown(event, keycode);
            }
        });
    }

    public Color getBgColor() {
        return bgColor;
    }

    public void act() {

    }
}
