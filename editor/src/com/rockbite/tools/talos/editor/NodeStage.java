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

    LegacyImporter legacyImporter;


    private String currentProjectPath = null;

    FileChooser fileChooser;


    public NodeStage (Skin skin) {
        this.skin = skin;
        stage = new Stage(new ScreenViewport(), new PolygonSpriteBatch());
    }

    public void init () {
        legacyImporter = new LegacyImporter(this);

        initActors();

        initFileChoosers();

//        loadDefaultProject();
        TalosMain.Instance().Project().newProject();

        initListeners();
    }


    public Stage getStage () {
        return stage;
    }

    public void resize (int width, int height) {
        stage.getViewport().update(width, height);
    }

    private void initFileChoosers() {
        fileChooser = new FileChooser(FileChooser.Mode.SAVE);
        fileChooser.setBackground(skin.getDrawable("window-noborder"));
    }

    private void initListeners() {
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if(keycode == Input.Keys.N && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    TalosMain.Instance().Project().newProject();
                }
                if(keycode == Input.Keys.O && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    openProjectAction();
                }
                if(keycode == Input.Keys.S && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    saveProjectAction();
                }

                return super.keyDown(event, keycode);
            }

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


    private void loadDefaultProject() {
        FileHandle fileHandle = Gdx.files.internal("samples/fire.tls");
        if (fileHandle.exists()) {
            TalosMain.Instance().Project().loadProject(fileHandle);
        } else {
            // empty stuff
            TalosMain.Instance().Project().createNewEmitter("emitter1");
        }
    }

    private void openProjectAction() {
        fileChooser.setMode(FileChooser.Mode.OPEN);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory() || pathname.getAbsolutePath().endsWith(".tls");
            }
        });
        fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);

        fileChooser.setListener(new FileChooserAdapter() {
            @Override
            public void selected (Array<FileHandle> file) {
                TalosMain.Instance().Project().loadProject(Gdx.files.absolute(file.first().file().getAbsolutePath()));
            }
        });

        stage.addActor(fileChooser.fadeIn());
    }

    private void saveProjectAction() {
        if(currentProjectPath == null) {
            saveAsProjectAction();
        } else {
            saveProject(currentProjectPath);
        }
    }

    private void saveAsProjectAction() {
        fileChooser.setMode(FileChooser.Mode.SAVE);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory() || pathname.getAbsolutePath().endsWith(".tls");
            }
        });
        fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);

        fileChooser.setListener(new FileChooserAdapter() {
            @Override
            public void selected(Array<FileHandle> file) {
                String path = file.first().file().getAbsolutePath();
                if(!path.endsWith(".tls")) {
                    if(path.indexOf(".") > 0) {
                        path = path.substring(0, path.indexOf("."));
                    }
                    path += ".tls";
                }
                saveProject(path);
            }
        });

        stage.addActor(fileChooser.fadeIn());
    }


    public void legacyImport() {
        fileChooser.setMode(FileChooser.Mode.OPEN);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileFilter(new FileChooser.DefaultFileFilter(fileChooser));
        fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);

        fileChooser.setListener(new FileChooserAdapter() {
            @Override
            public void selected (Array<FileHandle> file) {
                cleanData();
                legacyImporter.read(file.get(0));
                currentProjectPath = null;
            }
        });

        stage.addActor(fileChooser.fadeIn());
    }

    public void cleanData() {
        moduleBoardWidget.clearAll();
    }

    private void openProject(String path) {
        FileHandle fileHandle = Gdx.files.internal(path);
        openProject(fileHandle);
    }

    private void openProject(FileHandle fileHandle) {
        if(fileHandle.exists()) {
            cleanData();
            TalosMain.Instance().Project().loadProject(fileHandle);
            currentProjectPath = fileHandle.path();
        }
    }

    private void saveProject(String path) {
        FileHandle fileHandleWrite = Gdx.files.absolute(path);
        try {
            TalosMain.Instance().Project().saveProject(fileHandleWrite);
            currentProjectPath = path;
        } catch (Exception e) {
            Dialogs.showErrorDialog(stage, "Access Denied");
        }
    }

    public ModuleGraph getCurrentModuleGraph() {
        return TalosMain.Instance().Project().getCurrentModuleGraph();
    }

    public String getLocalPath() {
        try {
            return new File(this.getClass().getProtectionDomain().getCodeSource().getLocation()
                    .toURI()).getParent();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return "";
    }


    public void onEmitterRemoved (EmitterWrapper wrapper) {
        moduleBoardWidget.removeEmitter(wrapper);
        moduleBoardWidget.setCurrentEmitter(TalosMain.Instance().Project().getCurrentEmitterWrapper());
    }

    public void fileDrop(String[] paths, float x, float y) {
        moduleBoardWidget.fileDrop(paths, x, y);
    }


}
