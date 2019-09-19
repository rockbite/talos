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
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.kotcrab.vis.ui.widget.*;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.utils.GridRenderer;
import com.rockbite.tools.talos.runtime.ModuleGraph;
import com.rockbite.tools.talos.runtime.ParticleEffect;
import com.rockbite.tools.talos.runtime.ParticleEffectDescriptor;
import com.rockbite.tools.talos.editor.widgets.ui.ModuleBoardWidget;
import com.rockbite.tools.talos.editor.widgets.ui.PreviewWidget;
import com.rockbite.tools.talos.editor.widgets.ui.TimelineWidget;
import com.rockbite.tools.talos.runtime.ParticleSystem;

import java.io.File;
import java.io.FileFilter;
import java.net.URISyntaxException;

public class NodeStage {

    private Stage stage;

    TextureAtlas atlas;
    public Skin skin;

    public ModuleBoardWidget moduleBoardWidget;

    public ParticleSystem particleSystem;


    private ParticleEffectDescriptor particleEffectDescriptor;
    private Array<EmitterWrapper> emitterWrappers = new Array<>();

    public EmitterWrapper currentEmitterWrapper;

    LegacyImporter legacyImporter;

    ProjectSerializer projectSerializer;

    private String currentProjectPath = null;

    FileChooser fileChooser;


    public NodeStage (Skin skin) {
        this.skin = skin;
        stage = new Stage(new ScreenViewport(), new PolygonSpriteBatch());
    }

    public void init () {
        projectSerializer = new ProjectSerializer(this);
        legacyImporter = new LegacyImporter(this);

        initData();

        initActors();

        initFileChoosers();

        loadDefaultProject();

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
                    newProjectAction();
                }
                if(keycode == Input.Keys.O && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    openProjectAction();
                }
                if(keycode == Input.Keys.S && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    saveProjectAction();
                }

                return super.keyDown(event, keycode);
            }
        });
    }

    private void initData() {
        particleSystem = new ParticleSystem();
        particleEffectDescriptor = new ParticleEffectDescriptor();
        particleSystem.createEffect(particleEffectDescriptor);
    }

    private void initActors() {
        GridRenderer gridRenderer = new GridRenderer(stage);
        stage.addActor(gridRenderer);

        moduleBoardWidget = new ModuleBoardWidget(this);

        stage.addActor(moduleBoardWidget);
    }


    private void loadDefaultProject() {
        FileHandle fileHandle = Gdx.files.internal("samples/fire.tls");
        if(fileHandle.exists()) {
            projectSerializer.read(fileHandle);
        } else {
            // empty stuff
            createNewEmitter("emitter1");
        }

        TalosMain.Instance().UIStage().setEmitters(emitterWrappers);
    }

    private void newProjectAction() {
        newProject();
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
                openProject(file.first().file().getAbsolutePath());
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

    public void newProject() {
        cleanData();
        createNewEmitter("emitter1");
        TalosMain.Instance().UIStage().setEmitters(emitterWrappers);
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
                TalosMain.Instance().UIStage().setEmitters(emitterWrappers);
                currentProjectPath = null;
            }
        });

        stage.addActor(fileChooser.fadeIn());
    }

    public void cleanData() {
        particleSystem.clearEffect(particleSystem.getEffectDescriptors().get(0));
        currentEmitterWrapper = null;
        moduleBoardWidget.clearAll();
        emitterWrappers.clear();
    }

    private void openProject(String path) {
        FileHandle fileHandle = Gdx.files.internal(path);
        openProject(fileHandle);
    }

    private void openProject(FileHandle fileHandle) {
        if(fileHandle.exists()) {
            cleanData();
            projectSerializer.read(fileHandle);
            currentProjectPath = fileHandle.path();
            TalosMain.Instance().UIStage().setEmitters(emitterWrappers);
        }
    }

    private void saveProject(String path) {
        FileHandle fileHandleWrite = Gdx.files.absolute(path);
        try {
            projectSerializer.write(fileHandleWrite);
            currentProjectPath = path;
        } catch (Exception e) {
            Dialogs.showErrorDialog(stage, "Access Denied");
        }
    }

    public ModuleGraph getCurrentModuleGraph() {
        if(currentEmitterWrapper != null) {
            return currentEmitterWrapper.getGraph();
        } else {
            return null;
        }
    }

    public EmitterWrapper createNewEmitter(String emitterName) {
        EmitterWrapper emitterWrapper = new EmitterWrapper();
        emitterWrapper.setName(emitterName);

        ModuleGraph graph = particleSystem.createEmptyEmitter(particleEffectDescriptor);
        emitterWrapper.setModuleGraph(graph);

        emitterWrappers.add(emitterWrapper);
        currentEmitterWrapper = emitterWrapper;

        moduleBoardWidget.setCurrentEmitter(currentEmitterWrapper);

        return emitterWrapper;
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

    public Array<EmitterWrapper> getEmitterWrappers() {
        return emitterWrappers;
    }

    public void removeEmitter(EmitterWrapper wrapper) {
        int index = emitterWrappers.indexOf(wrapper, true);
        index--;
        if(index < 0) index = 0;

        ParticleEffectDescriptor effect = particleSystem.getEffectDescriptors().get(0);
        particleSystem.removeEmitter(effect, wrapper.getEmitter());

        moduleBoardWidget.removeEmitter(wrapper);

        emitterWrappers.removeValue(wrapper, true);

        if(emitterWrappers.size > 0) {
            currentEmitterWrapper = emitterWrappers.get(index);
        } else {
            currentEmitterWrapper = null;
        }

        moduleBoardWidget.setCurrentEmitter(currentEmitterWrapper);
    }

    public void fileDrop(String[] paths, float x, float y) {
        moduleBoardWidget.fileDrop(paths, x, y);
    }

    public ParticleSystem getParticleSystem () {
        return particleSystem;
    }
}
