package com.talosvfx.talos.editor.addons.shader;

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

public class ShaderAddon implements IAddon {

    public static ShaderProject SHADER_PROJECT;

    @Override
    public void init () {
        SHADER_PROJECT = new ShaderProject(this);
        buildUI();
    }

    private void buildUI () {

    }

    @Override
    public void initUIContent () {
        TalosMain.Instance().UIStage().swapToAddonContent(null, null, null);
        TalosMain.Instance().disableNodeStage();

        // now need to disable some menu tabs
        TalosMain.Instance().UIStage().Menu().disableTalosSpecific();
    }

    @Override
    public boolean projectFileDrop (FileHandle handle) {
        return false;
    }

    @Override
    public IProject getProjectType () {
        return null;
    }

    @Override
    public void announceLocalSettings (SettingsDialog settingsDialog) {

    }

    @Override
    public void buildMenu (MenuBar menuBar) {
        Menu menu = new Menu("Shader Editor");

        MenuItem newFile = new MenuItem("New Shader");
        menu.addItem(newFile);
        MenuItem openFile = new MenuItem("Open Shader");
        menu.addItem(openFile);


        newFile.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                TalosMain.Instance().ProjectController().newProject(SHADER_PROJECT);
            }
        });

        openFile.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
               //
            }
        });

        menuBar.addMenu(menu);
    }
}
