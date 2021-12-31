package com.talosvfx.talos.editor.addons.scene;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.SplitPane;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuBar;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.VisSplitPane;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.IAddon;
import com.talosvfx.talos.editor.addons.scene.events.*;
import com.talosvfx.talos.editor.addons.scene.widgets.BottomPanel;
import com.talosvfx.talos.editor.addons.scene.widgets.HierarchyWidget;
import com.talosvfx.talos.editor.addons.scene.widgets.ProjectExplorerWidget;
import com.talosvfx.talos.editor.addons.scene.widgets.PropertyPanel;
import com.talosvfx.talos.editor.dialogs.SettingsDialog;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.project.FileTracker;
import com.talosvfx.talos.editor.project.IProject;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;

public class SceneEditorAddon implements IAddon {

    public static SceneEditorProject SE;
    public SceneEditorWorkspace workspace;
    public HierarchyWidget hierarchy;
    public ProjectExplorerWidget projectExplorer;
    public PropertyPanel propertyPanel;

    public FileTracker.Tracker assetTracker;
    private Table customLayoutTable;
    public Table workspaceContainer;

    @Override
    public void init () {
        SE = new SceneEditorProject(this);

        registerEvents();
        buildUI();

        assetTracker = handle -> workspace.updateAsset(handle);
    }

    private void registerEvents () {
        Notifications.addEventToPool(PropertyHolderSelected.class);
        Notifications.addEventToPool(GameObjectSelectionChanged.class);
        Notifications.addEventToPool(GameObjectCreated.class);
        Notifications.addEventToPool(ComponentUpdated.class);
        Notifications.addEventToPool(GameObjectDeleted.class);
        Notifications.addEventToPool(GameObjectNameChanged.class);
        Notifications.addEventToPool(LayerListUpdated.class);
        Notifications.addEventToPool(CameraDataUpdated.class);
    }

    @Override
    public void buildMenu(MenuBar menuBar) {
        Menu mainMenu = new Menu("Scene Editor");

        MenuItem newProject = new MenuItem("New Project");
        mainMenu.addItem(newProject);
        MenuItem openProject = new MenuItem("Open Project");
        mainMenu.addItem(openProject);

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

        menuBar.addMenu(mainMenu);
    }

    private void buildUI () {
        workspace = SceneEditorWorkspace.getInstance();
        workspace.setAddon(this);

        propertyPanel = new PropertyPanel();
        hierarchy = new HierarchyWidget();
        projectExplorer = new ProjectExplorerWidget();

        customLayoutTable = new Table();
        makeLayout(customLayoutTable);
    }

    private void makeLayout(Table container) {
        Skin skin = TalosMain.Instance().getSkin();

        Table leftPart = new Table();
        Table midPart = new Table();
        workspaceContainer = new Table();
        SplitPane horizontalPane = new SplitPane(leftPart, propertyPanel, false, skin, "timeline");
        SplitPane verticalPane = new SplitPane(midPart, projectExplorer, true, skin, "timeline");
        SplitPane midPane = new SplitPane(hierarchy, workspaceContainer, false, skin, "timeline");

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
        if(handle.extension().equals("png")) {
            // import it
            FileHandle importedAsset = workspace.importAsset(handle);

            // track it
            TalosMain.Instance().FileTracker().trackFile(importedAsset, assetTracker);

            // add new game object to the scene
            Vector2 sceneCords = workspace.getMouseCordsOnScene();
            workspace.createSpriteObject(importedAsset, sceneCords);

            return true;
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
}
