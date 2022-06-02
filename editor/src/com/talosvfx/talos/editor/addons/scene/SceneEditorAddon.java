package com.talosvfx.talos.editor.addons.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.SplitPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuBar;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.VisSplitPane;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.IAddon;
import com.talosvfx.talos.editor.addons.scene.apps.EditorApps;
import com.talosvfx.talos.editor.addons.scene.events.*;
import com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter;
import com.talosvfx.talos.editor.addons.scene.widgets.HierarchyWidget;
import com.talosvfx.talos.editor.addons.scene.widgets.ProjectExplorerWidget;
import com.talosvfx.talos.editor.addons.scene.widgets.PropertyPanel;
import com.talosvfx.talos.editor.addons.scene.widgets.SEPropertyPanel;
import com.talosvfx.talos.editor.dialogs.SettingsDialog;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.project.IProject;
import com.talosvfx.talos.editor.project.ProjectController;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;

public class SceneEditorAddon implements IAddon {

    public static SceneEditorProject SE;
    public SceneEditorWorkspace workspace;
    public HierarchyWidget hierarchy;
    public ProjectExplorerWidget projectExplorer;
    public SEPropertyPanel propertyPanel;

    private Table customLayoutTable;
    public Table workspaceContainer;

    public AssetImporter assetImporter;
    public SEAssetProvider assetProvider;

    public com.talosvfx.talos.editor.addons.scene.dialogs.SettingsDialog settingsDialog;

    @Override
    public void init () {
        SE = new SceneEditorProject(this);

        assetProvider = new SEAssetProvider();
        assetImporter = new AssetImporter();

        registerEvents();
        buildUI();
    }

    private void registerEvents () {
        Notifications.addEventToPool(PropertyHolderSelected.class);
        Notifications.addEventToPool(GameObjectSelectionChanged.class);
        Notifications.addEventToPool(GameObjectCreated.class);
        Notifications.addEventToPool(ComponentUpdated.class);
        Notifications.addEventToPool(GameObjectDeleted.class);
        Notifications.addEventToPool(GameObjectNameChanged.class);
        Notifications.addEventToPool(LayerListUpdated.class);
        Notifications.addEventToPool(ProjectOpened.class);
        Notifications.addEventToPool(ProjectDirectoryContentsChanged.class);
        Notifications.addEventToPool(PropertyHolderEdited.class);
        Notifications.addEventToPool(AssetPathChanged.class);
    }

    @Override
    public void buildMenu(MenuBar menuBar) {
        Menu mainMenu = new Menu("Scene Editor");

        MenuItem newProject = new MenuItem("New Project");
        mainMenu.addItem(newProject);
        MenuItem openProject = new MenuItem("Open Project");
        mainMenu.addItem(openProject);
        mainMenu.addSeparator();
        MenuItem projectSettings = new MenuItem("Project Settings");
        mainMenu.addItem(projectSettings);

        newProject.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                TalosMain.Instance().ProjectController().newProject(SE);
            }
        });

        openProject.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                TalosMain.Instance().UIStage().openProjectAction(SE);
            }
        });

        projectSettings.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                if(TalosMain.Instance().ProjectController().getProject() == SE) {
                    showSettingsDialog();
                }
            }
        });

        menuBar.addMenu(mainMenu);
    }

    public void showSettingsDialog() {
        TalosMain.Instance().UIStage().openDialog(settingsDialog);
        settingsDialog.initData();
    }

    @Override
    public void dispose () {
        workspace.dispose();
    }

    private void buildUI () {
        workspace = SceneEditorWorkspace.getInstance();
        workspace.setAddon(this);

        propertyPanel = new SEPropertyPanel();
        hierarchy = new HierarchyWidget();
        projectExplorer = new ProjectExplorerWidget();

        customLayoutTable = new Table();
        makeLayout(customLayoutTable);

        settingsDialog = new com.talosvfx.talos.editor.addons.scene.dialogs.SettingsDialog();
    }

    private void makeLayout(Table container) {
        Skin skin = TalosMain.Instance().getSkin();

        Table leftPart = new Table();
        Table midPart = new Table();
        workspaceContainer = new Table();
        VisSplitPane horizontalPane = new VisSplitPane(leftPart, propertyPanel, false);
        VisSplitPane verticalPane = new VisSplitPane(midPart, projectExplorer, true);
        VisSplitPane midPane = new VisSplitPane(hierarchy, workspaceContainer, false);

        leftPart.add(verticalPane).grow();
        midPart.add(midPane).grow();

        horizontalPane.setSplitAmount(0.8f);
        verticalPane.setSplitAmount(0.72f);
        midPane.setSplitAmount(0.25f);

        Drawable workspaceBg = ColorLibrary.obtainBackground(skin, ColorLibrary.BackgroundColor.RED);
        Drawable panelBg = ColorLibrary.obtainBackground(skin, ColorLibrary.BackgroundColor.SUPER_DARK_GRAY);

        workspaceContainer.setBackground(workspaceBg);
        propertyPanel.setBackground(panelBg);
        hierarchy.setBackground(panelBg);
        projectExplorer.setBackground(panelBg);

        workspaceContainer.add(workspace).grow();

        container.add(horizontalPane).grow();
    }

    @Override
    public void initUIContent () {
        TalosMain.Instance().UIStage().swapToAddonContent(null, null, null);
        TalosMain.Instance().disableNodeStage();
        TalosMain.Instance().UIStage().showCustomLayout(customLayoutTable);

        // now need to disable some menu tabs
        TalosMain.Instance().UIStage().Menu().disableTalosSpecific();

        TalosMain.Instance().UIStage().getStage().setKeyboardFocus(workspace);
    }

    @Override
    public boolean projectFileDrop (FileHandle handle) {

        if(TalosMain.Instance().ProjectController().getProject() != SE) return false;

        Vector2 vec = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        workspace.screenToLocalCoordinates(vec);
        Actor hit = workspace.hit(vec.x, vec.y, false);
        if(hit != null) {
            // workspace is hit
            if (AssetImporter.attemptToImport(handle, true) != null) {
                return true;
            }
        } else {
            vec = new Vector2(Gdx.input.getX(), Gdx.input.getY());
            projectExplorer.screenToLocalCoordinates(vec);
            hit = projectExplorer.hit(vec.x, vec.y, false);

            if(hit != null) {
                // File Explorer is hit
                if (AssetImporter.attemptToImport(handle, false) != null) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public IProject getProjectType () {
        return SE;
    }

    @Override
    public void announceLocalSettings (SettingsDialog settingsDialog) {
        settingsDialog.addPathSetting("Scene Projects Path", "sceneEditorProjectsPath");
    }

    public static SceneEditorAddon get() {
        // todo: add some null checks
        return ((SceneEditorProject)TalosMain.Instance().Project()).sceneEditorAddon;
    }

    public EditorApps Apps() {
        return EditorApps.getInstance();
    }
}
