package com.rockbite.tools.talos.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.serialization.ProjectSerializer;
import com.rockbite.tools.talos.editor.utils.GridRenderer;
import com.rockbite.tools.talos.runtime.ModuleGraph;
import com.rockbite.tools.talos.editor.widgets.ui.ModuleBoardWidget;

import java.io.File;
import java.io.FileFilter;
import java.net.URISyntaxException;

public class NodeStage {

    private Stage stage;

    TextureAtlas atlas;
    public Skin skin;

    public ModuleBoardWidget moduleBoardWidget;


    public NodeStage (Skin skin) {
        this.skin = skin;
        stage = new Stage(new ScreenViewport(), new PolygonSpriteBatch());
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
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                if (button == 1)
                    moduleBoardWidget.showPopup();

                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
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

    public ModuleGraph getCurrentModuleGraph() {
        return TalosMain.Instance().Project().getCurrentModuleGraph();
    }


    public void onEmitterRemoved (EmitterWrapper wrapper) {
        moduleBoardWidget.removeEmitter(wrapper);
        moduleBoardWidget.setCurrentEmitter(TalosMain.Instance().Project().getCurrentEmitterWrapper());
    }

    public void fileDrop(String[] paths, float x, float y) {
        moduleBoardWidget.fileDrop(paths, x, y);
    }


}
