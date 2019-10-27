/*******************************************************************************
 * Copyright 2019 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.rockbite.tools.talos.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
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

    private Image selectionRect;

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
            Vector2 startPos = new Vector2();
            Vector2 tmp = new Vector2();
            Rectangle rectangle = new Rectangle();

            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                TalosMain.Instance().UIStage().getStage().unfocusAll();

                wasDragged = false;

                if(button == 2) {
                    selectionRect.setVisible(true);
                    selectionRect.setSize(0, 0);
                    startPos.set(x, y);
                }

                TalosMain.Instance().getCameraController().touchDown(Gdx.input.getX(), Gdx.input.getY(), pointer, button);

                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                if(event.isHandled()) return;

                wasDragged = true;

                if(selectionRect.isVisible()) {
                    tmp.set(x, y);
                    tmp.sub(startPos);
                    if(tmp.x < 0) {
                        rectangle.setX(x);
                    } else {
                        rectangle.setX(startPos.x);
                    }
                    if(tmp.y < 0) {
                        rectangle.setY(y);
                    } else {
                        rectangle.setY(startPos.y);
                    }
                    rectangle.setWidth(Math.abs(tmp.x));
                    rectangle.setHeight(Math.abs(tmp.y));

                    selectionRect.setPosition(rectangle.x, rectangle.y);
                    selectionRect.setSize(rectangle.getWidth(), rectangle.getHeight());
                }

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

                if (button == 1 && !event.isHandled()) {
                    moduleBoardWidget.showPopup();
                }

                if(button == 2) {
                    moduleBoardWidget.userSelectionApply(rectangle);
                }

                selectionRect.setVisible(false);

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

                if(keycode == Input.Keys.A && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    moduleBoardWidget.selectAllModules();
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

        selectionRect = new Image(skin.getDrawable("orange_row"));
        selectionRect.setSize(0, 0);
        selectionRect.setVisible(false);
        stage.addActor(selectionRect);
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
