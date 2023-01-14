package com.talosvfx.talos.editor.widgets.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.kotcrab.vis.ui.widget.*;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.UIStage;

@Deprecated
public class MainMenuLegacy extends Table {

    UIStage stage;
    private MenuItem saveProject;
    private MenuItem export;
    private MenuItem exportAs;
    private MenuItem saveAsProject;
    private Menu modulesMenu;
    private MenuItem removeSelectedModules;
    private MenuItem createModule;
    private MenuItem groupSelectedModules;
    private MenuItem ungroupSelectedModules;
    private PopupMenu openRecentPopup;

    public MainMenuLegacy(UIStage stage) {
        setSkin(stage.getSkin());
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

//        TalosMain.Instance().Addons().buildMenu(menuBar);

        Menu helpMenu = new Menu("Help");
        MenuItem about = new MenuItem("About");
        helpMenu.addItem(about);
        menuBar.addMenu(helpMenu);

        about.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                VisDialog dialog = Dialogs.showOKDialog(stage.getStage(), "About Talos 1.5.0", "Talos is a an open source node based FX and Shader editor");
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

        final MenuItem newProject = new MenuItem("New TalosProject", icon("ic-file-new"));
        final MenuItem openProject = new MenuItem("Open TalosProject", icon("ic-folder"));
        MenuItem openRecent = new MenuItem("Open Recent", icon("ic-folder-recent"));
        saveProject = new MenuItem("Save", icon("ic-save"));
        export = new MenuItem("Export" , icon("ic-download"));
        exportAs = new MenuItem("Export As");
        MenuItem examples = new MenuItem("Examples");

        openRecentPopup = new PopupMenu();
        openRecent.setSubMenu(openRecentPopup);

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
        saveAsProject = new MenuItem("Save As", icon("ic-save-aster"));
        MenuItem exitApp = new MenuItem("Exit");

        projectMenu.addItem(newProject);
        projectMenu.addItem(openProject);
        projectMenu.addItem(openRecent);
        projectMenu.addItem(saveProject);
        projectMenu.addItem(saveAsProject);
        projectMenu.addItem(export);
        projectMenu.addItem(exportAs);
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
//                TalosMain.Instance().NodeStage().moduleBoardWidget.deleteSelectedWrappers();
            }
        });

        groupSelectedModules.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
//                TalosMain.Instance().NodeStage().moduleBoardWidget.createGroupFromSelectedWrappers();
            }
        });

        ungroupSelectedModules.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
//                TalosMain.Instance().NodeStage().moduleBoardWidget.ungroupSelectedWrappers();
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
                Gdx.app.exit();
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
//                        TalosMain.Instance().ProjectController().newProject(ProjectController.TLS);
                    }
                }
                if(keycode == Input.Keys.O && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                    if(!openProject.isDisabled()) {
                        stage.openProjectAction();
                    }
                }

                return super.keyDown(event, keycode);
            }
        });

        TalosMain.Instance().ProjectController().updateRecentsList();
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

    public void updateRecentsList(Array<String> list) {
        openRecentPopup.clear();

        for(String path: list) {
            final FileHandle handle = Gdx.files.absolute(path);
            if(!handle.exists()) continue;;
            String name = handle.name();
            MenuItem item = new MenuItem(name);
            item.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    super.clicked(event, x, y);
                    if(handle.extension().equals("tls")) {
//                        TalosMain.Instance().ProjectController().setProject(ProjectController.TLS);
                        TalosMain.Instance().ProjectController().loadProject(handle);
                    } else {
//                        TalosMain.Instance().Addons().projectFileDrop(handle);
                    }
                }
            });
            openRecentPopup.addItem(item);
        }
    }

    private Image icon(String name) {
        return new Image(getSkin().getDrawable(name));
    }
}
