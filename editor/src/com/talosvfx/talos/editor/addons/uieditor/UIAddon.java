package com.talosvfx.talos.editor.addons.uieditor;

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

public class UIAddon implements IAddon {

    public static UIProject UI;

    private UIWorkspace workspace;

    @Override
    public void init () {
        UI = new UIProject(this);

        buildUI();
    }

    private void buildUI () {
        workspace = new UIWorkspace(this);
    }

    @Override
    public void initUIContent () {
        TalosMain.Instance().UIStage().swapToAddonContent(null, workspace, null);
        TalosMain.Instance().disableNodeStage();

        // now need to disable some menu tabs
        TalosMain.Instance().UIStage().Menu().disableTalosSpecific();

        //TalosMain.Instance().UIStage().getStage().setKeyboardFocus(workspace);
    }

    @Override
    public boolean projectFileDrop (FileHandle handle) {
        return false;
    }

    @Override
    public IProject getProjectType () {
        return UI;
    }

    @Override
    public void announceLocalSettings (SettingsDialog settingsDialog) {

    }

    @Override
    public void buildMenu (MenuBar menuBar) {
        Menu menu = new Menu("UI");

        MenuItem newProject = new MenuItem("New Project");
        menu.addItem(newProject);
        MenuItem openProject = new MenuItem("Open Project");
        menu.addItem(openProject);

        newProject.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                TalosMain.Instance().ProjectController().newProject(UI);
            }
        });

        openProject.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                TalosMain.Instance().UIStage().openProjectAction(UI);
            }
        });

        menuBar.addMenu(menu);
    }
}
