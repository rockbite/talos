package com.talosvfx.talos.editor.addons.scene;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuBar;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.IAddon;
import com.talosvfx.talos.editor.dialogs.SettingsDialog;
import com.talosvfx.talos.editor.project.IProject;

public class SceneEditorAddon implements IAddon {

    public static SceneEditorProject SE;
    SceneEditorWorkspace workspace;


    @Override
    public void init () {
        SE = new SceneEditorProject(this);

        buildUI();
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
    }

    @Override
    public void initUIContent () {
        TalosMain.Instance().UIStage().swapToAddonContent(null, workspace, null);
        TalosMain.Instance().disableNodeStage();

        // now need to disable some menu tabs
        TalosMain.Instance().UIStage().Menu().disableTalosSpecific();

        TalosMain.Instance().UIStage().getStage().setKeyboardFocus(workspace);
    }

    @Override
    public boolean projectFileDrop (FileHandle handle) {
        return false;
    }

    @Override
    public IProject getProjectType () {
        return SE;
    }

    @Override
    public void announceLocalSettings (SettingsDialog settingsDialog) {

    }
}
