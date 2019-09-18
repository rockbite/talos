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

public class MainStage extends Stage {

    TextureAtlas atlas;
    public Skin skin;

    public ModuleBoardWidget moduleBoardWidget;

    public ParticleSystem particleSystem;

    PreviewWidget previewWidget;

    private ParticleEffectDescriptor particleEffectDescriptor;
    private Array<EmitterWrapper> emitterWrappers = new Array<>();

    public EmitterWrapper currentEmitterWrapper;

    private TimelineWidget timelineWidget;

    LegacyImporter legacyImporter;

    ProjectSerializer projectSerializer;

    private String currentProjectPath = null;

    FileChooser fileChooser;

    private PopupMenu examplesPopup;

    public MainStage() {
        super(new ScreenViewport(),
                new PolygonSpriteBatch());

        Gdx.input.setInputProcessor(this);

        atlas = new TextureAtlas(Gdx.files.internal("skin/uiskin.atlas"));
        skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
        skin.addRegions(atlas);

        VisUI.load(skin);

        projectSerializer = new ProjectSerializer(this);
        legacyImporter = new LegacyImporter(this);

        initData();

        initActors();

        initFileChoosers();

        loadDefaultProject();

        initListeners();
    }

    private void initFileChoosers() {
        fileChooser = new FileChooser(FileChooser.Mode.SAVE);
        fileChooser.setBackground(skin.getDrawable("window-noborder"));
    }

    private void initListeners() {
        addListener(new InputListener() {
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
        Table mainTable = new Table();
        mainTable.setSkin(skin);
        mainTable.setFillParent(true);

        moduleBoardWidget = new ModuleBoardWidget(this);

        previewWidget = new PreviewWidget();
        previewWidget.setParticleSystem(particleSystem);

        Table topTable = new Table();
        topTable.setBackground(skin.getDrawable("button-main-menu"));
        Table contentTable = new Table();
        mainTable.add(topTable).left().growX();
        mainTable.row();
        mainTable.add(contentTable).grow();

        MenuBar menuBar = new MenuBar();
        Menu projectMenu = new Menu("File");
        menuBar.addMenu(projectMenu);
        Menu helpMenu = new Menu("Help");
        MenuItem about = new MenuItem("About");
        helpMenu.addItem(about);
        menuBar.addMenu(helpMenu);

        about.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                VisDialog dialog = Dialogs.showOKDialog (MainStage.this, "About Talos 1.0.2", "Talos Particle Editor 1.0.2");
            }
        });

        MenuItem newProject = new MenuItem("New Project");
        MenuItem openProject = new MenuItem("Open Project");
        MenuItem saveProject = new MenuItem("Save Project");
        MenuItem examples = new MenuItem("Examples");
        MenuItem importItem = new MenuItem("Legacy Import");
        examplesPopup = new PopupMenu();
        examples.setSubMenu(examplesPopup);
        initExampleList(examplesPopup);
        MenuItem saveAsProject = new MenuItem("Save As Project");
        MenuItem exitApp = new MenuItem("Exit");

        projectMenu.addItem(newProject);
        projectMenu.addItem(openProject);
        projectMenu.addItem(saveProject);
        projectMenu.addItem(saveAsProject);
        projectMenu.addSeparator();
        projectMenu.addItem(examples);
        projectMenu.addItem(importItem);
        projectMenu.addSeparator();
        projectMenu.addItem(exitApp);

        newProject.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                newProjectAction();
            }
        });

        openProject.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                openProjectAction();
            }
        });

        saveProject.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                saveProjectAction();
            }
        });

        saveAsProject.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                saveAsProjectAction();
            }
        });

        importItem.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                legacyImport();
            }
        });

        exitApp.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                System.exit(0);
            }
        });


        topTable.add(menuBar.getTable()).left().expand();

        Table midTable = new Table();
        Table bottomTable = new Table();

        bottomTable.setSkin(skin);
        Table timelineContainer = new Table();
        Table libraryContainer = new Table();
        VisSplitPane bottomPane = new VisSplitPane(timelineContainer, libraryContainer, false);
        timelineWidget = new TimelineWidget(skin);
        timelineContainer.add(timelineWidget).grow().expand().fill();
        bottomTable.add(bottomPane).expand().grow();

        VisSplitPane verticalPane = new VisSplitPane(midTable, bottomTable, true);
        contentTable.add(verticalPane).expand().grow().fill();
        verticalPane.setMaxSplitAmount(0.8f);
        verticalPane.setMinSplitAmount(0.2f);
        verticalPane.setSplitAmount(0.7f);

        Table leftTable = new Table(); leftTable.setSkin(skin);
        leftTable.add(previewWidget);
        Table rightTable = new Table(); rightTable.setSkin(skin);
        rightTable.add(moduleBoardWidget).grow();
        VisSplitPane horizontalPane = new VisSplitPane(leftTable, rightTable, false);
        midTable.add(horizontalPane).expand().grow().fill();
        horizontalPane.setMaxSplitAmount(0.8f);
        horizontalPane.setMinSplitAmount(0.2f);
        horizontalPane.setSplitAmount(0.3f);

        addActor(mainTable);
    }

    private void initExampleList(PopupMenu examples) {
        FileHandle dir = Gdx.files.internal("samples");
        for(final FileHandle file: dir.list()) {
            if(file.extension().equals("tls")) {
                MenuItem item  = new MenuItem(file.name());
                examples.addItem(item);

                item.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        super.clicked(event, x, y);
                        openProject(file);
                        currentProjectPath = null;
                    }
                });
            }
        }
    }

    private void loadDefaultProject() {
        FileHandle fileHandle = Gdx.files.internal("samples/default.tls");
        if(fileHandle.exists()) {
            projectSerializer.read(fileHandle);
        } else {
            // empty stuff
            createNewEmitter("emitter1");
        }

        timelineWidget.setEmitters(emitterWrappers);
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

        addActor(fileChooser.fadeIn());
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

        addActor(fileChooser.fadeIn());
    }

    public void newProject() {
        cleanData();
        createNewEmitter("emitter1");
        timelineWidget.setEmitters(emitterWrappers);
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
                timelineWidget.setEmitters(emitterWrappers);
                currentProjectPath = null;
            }
        });

        addActor(fileChooser.fadeIn());
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
            timelineWidget.setEmitters(emitterWrappers);
        }
    }

    private void saveProject(String path) {
        FileHandle fileHandleWrite = Gdx.files.absolute(path);
        try {
            projectSerializer.write(fileHandleWrite);
            currentProjectPath = path;
        } catch (Exception e) {
            Dialogs.showErrorDialog(this, "Access Denied");
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
}
