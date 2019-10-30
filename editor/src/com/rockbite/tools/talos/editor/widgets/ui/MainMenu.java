package com.rockbite.tools.talos.editor.widgets.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.kotcrab.vis.ui.widget.*;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.UIStage;
import com.rockbite.tools.talos.editor.project.ProjectController;

public class MainMenu extends Table {

    UIStage stage;
    private MenuItem saveProject;
    private MenuItem export;
    private MenuItem saveAsProject;
    private Menu modulesMenu;
    private MenuItem removeSelectedModules;
    private MenuItem createModule;
    private MenuItem groupSelectedModules;
    private MenuItem ungroupSelectedModules;

    public MainMenu(UIStage stage) {
        this.stage = stage;

        setBackground(stage.getSkin().getDrawable("button-main-menu"));
    }

    public void build() {
        clearChildren();

        MenuBar menuBar = new MenuBar();
        Menu projectMenu = new Menu("File");
        menuBar.addMenu(projectMenu);
        modulesMenu = new Menu("Modules");
        menuBar.addMenu(modulesMenu);

        TalosMain.Instance().Addons().buildMenu(menuBar);

        Menu helpMenu = new Menu("Help");
        MenuItem about = new MenuItem("About");
        helpMenu.addItem(about);
        menuBar.addMenu(helpMenu);

        about.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                VisDialog dialog = Dialogs.showOKDialog(stage.getStage(), "About Talos 1.0.6", "When blue and yellow meet in the west, the silver cat feeds");
            }
        });

        createModule = new MenuItem("Create Module");
        PopupMenu createPopup = stage.createModuleListPopup();
        createModule.setSubMenu(createPopup);
        removeSelectedModules = new MenuItem("Remove Selected").setShortcut(Input.Keys.DEL);
        groupSelectedModules = new MenuItem("Group Selected").setShortcut(Input.Keys.CONTROL_LEFT, Input.Keys.G);
        ungroupSelectedModules = new MenuItem("Ungroup Selected").setShortcut(Input.Keys.CONTROL_LEFT, Input.Keys.U);
        modulesMenu.addItem(createModule);
        modulesMenu.addItem(removeSelectedModules);
        modulesMenu.addItem(groupSelectedModules);

        final MenuItem newProject = new MenuItem("New TalosProject");
        final MenuItem openProject = new MenuItem("Open TalosProject");
        saveProject = new MenuItem("Save");
        export = new MenuItem("Export");
        MenuItem examples = new MenuItem("Examples");

        MenuItem legacy = new MenuItem("Legacy");
        PopupMenu legacyPopup = new PopupMenu();
        MenuItem legacyImportItem = new MenuItem("Import");
        MenuItem legacyBatchImportItem = new MenuItem("Batch Convert");
        legacyPopup.addItem(legacyImportItem);
        legacyPopup.addItem(legacyBatchImportItem);
        legacy.setSubMenu(legacyPopup);

        MenuItem settings = new MenuItem("Preferences");

        PopupMenu examplesPopup = new PopupMenu();
        examples.setSubMenu(examplesPopup);
        stage.initExampleList(examplesPopup);
        saveAsProject = new MenuItem("Save As");
        MenuItem exitApp = new MenuItem("Exit");

        projectMenu.addItem(newProject);
        projectMenu.addItem(openProject);
        projectMenu.addItem(saveProject);
        projectMenu.addItem(saveAsProject);
        projectMenu.addItem(export);
        projectMenu.addSeparator();
        projectMenu.addItem(examples);
        projectMenu.addItem(legacy);
        projectMenu.addSeparator();
        projectMenu.addItem(settings);
        projectMenu.addSeparator();
        projectMenu.addItem(exitApp);

        removeSelectedModules.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                TalosMain.Instance().NodeStage().moduleBoardWidget.deleteSelectedWrappers();
            }
        });

        groupSelectedModules.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                TalosMain.Instance().NodeStage().moduleBoardWidget.createGroupFromSelectedWrappers();
            }
        });

        ungroupSelectedModules.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                TalosMain.Instance().NodeStage().moduleBoardWidget.ungroupSelectedWrappers();
            }
        });

        newProject.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                stage.newProjectAction();
            }
        });

        openProject.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                stage.openProjectAction();
            }
        });

        saveProject.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                if(!saveProject.isDisabled()) stage.saveProjectAction();
            }
        });

        export.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                stage.exportAction();
            }
        });

        saveAsProject.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                stage.saveAsProjectAction();
            }
        });

        legacyImportItem.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                stage.legacyImportAction();
            }
        });

        legacyBatchImportItem.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                stage.legacyBatchConvertAction();
            }
        });

        exitApp.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                System.exit(0);
            }
        });
        settings.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                stage.getStage().addActor(stage.settingsDialog.fadeIn());
            }
        });

        add(menuBar.getTable()).left().grow();


        // adding key listeners for menu items
        stage.getStage().addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if(keycode == Input.Keys.N && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    if(!newProject.isDisabled()) {
                        TalosMain.Instance().ProjectController().newProject(ProjectController.TLS);
                    }
                }
                if(keycode == Input.Keys.O && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    if(!openProject.isDisabled()) {
                        stage.openProjectAction();
                    }
                }
                if(keycode == Input.Keys.S && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    if(!saveProject.isDisabled()) {
                        stage.saveProjectAction();
                    }
                }

                return super.keyDown(event, keycode);
            }
        });
    }

    public void disableTalosSpecific() {
        disableItem(removeSelectedModules);
        disableItem(createModule);
        disableItem(groupSelectedModules);
        disableItem(ungroupSelectedModules);
    }

    public void disableItem(MenuItem item) {
        item.setDisabled(true);
    }

    public void enableItem(MenuItem item) {
        item.setDisabled(false);
    }

    public void restore() {
        enableItem(removeSelectedModules);
        enableItem(createModule);
        enableItem(groupSelectedModules);
        enableItem(ungroupSelectedModules);
    }
}
