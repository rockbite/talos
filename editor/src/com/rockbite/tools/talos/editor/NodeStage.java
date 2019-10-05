package com.rockbite.tools.talos.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.FocusManager;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.utils.GridRenderer;
import com.rockbite.tools.talos.runtime.ParticleEmitterDescriptor;
import com.rockbite.tools.talos.editor.widgets.ui.ModuleBoardWidget;

public class NodeStage {

    private Stage stage;

    TextureAtlas atlas;
    public Skin skin;

    public ModuleBoardWidget moduleBoardWidget;

    public NodeStage (Skin skin) {
        this.skin = skin;
        stage = new Stage(new ScreenViewport(), new PolygonSpriteBatch());
    }

    public Skin getSkin() {
        return skin;
    }

    public void init () {
        initActors();

        initListeners();
    }


    public Stage getStage () {
        return stage;
    }

    public void resize (int width, int height) {
        stage.getViewport().update(width, height);
    }

    private void initListeners() {
        stage.addListener(new InputListener() {

            boolean wasDragged;

            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                TalosMain.Instance().UIStage().getStage().unfocusAll();
                if (button == 1 && !event.isHandled()) {
                    moduleBoardWidget.showPopup();
                }
                wasDragged = false;

                TalosMain.Instance().getCameraController().touchDown(Gdx.input.getX(), Gdx.input.getY(), pointer, button);

                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                if(event.isHandled()) return;

                wasDragged = true;

                super.touchDragged(event, x, y, pointer);

                TalosMain.Instance().getCameraController().touchDragged(Gdx.input.getX(), Gdx.input.getY(), pointer);
            }

            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);

                if(!event.isHandled() && button == 0) {
                    FocusManager.resetFocus(getStage());
                    moduleBoardWidget.clearSelection();
                }

                TalosMain.Instance().getCameraController().touchUp(Gdx.input.getX(), Gdx.input.getY(), pointer, button);
            }

            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if(keycode == Input.Keys.G && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    moduleBoardWidget.createGroupFromSelectedWrappers();
                }

                if(keycode == Input.Keys.U && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    moduleBoardWidget.ungroupSelectedWrappers();
                }

                if(keycode == Input.Keys.C && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    moduleBoardWidget.copySelectedModules();
                }

                if(keycode == Input.Keys.V && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    moduleBoardWidget.pasteFromClipboard();
                }

                return super.keyDown(event, keycode);
            }
        });
    }


    private void initActors() {
        GridRenderer gridRenderer = new GridRenderer(stage);
        stage.addActor(gridRenderer);

        moduleBoardWidget = new ModuleBoardWidget(this);

        stage.addActor(moduleBoardWidget);
    }


    public void cleanData() {
        moduleBoardWidget.clearAll();
    }

    public ParticleEmitterDescriptor getCurrentModuleGraph() {
        return TalosMain.Instance().Project().getCurrentModuleGraph();
    }


    public void onEmitterRemoved (ParticleEmitterWrapper wrapper) {
        moduleBoardWidget.removeEmitter(wrapper);
        moduleBoardWidget.setCurrentEmitter(TalosMain.Instance().Project().getCurrentEmitterWrapper());
    }

    public void fileDrop(String[] paths, float x, float y) {
        moduleBoardWidget.fileDrop(paths, x, y);
    }


}
