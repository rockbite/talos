package com.rockbite.tools.talos.editor.addons.bvb;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.addons.IAddon;
import com.rockbite.tools.talos.editor.dialogs.SettingsDialog;
import com.rockbite.tools.talos.editor.project.IProject;

public class BvBAddon implements IAddon {

    public static BvbProject BVB;

    BvBWorkspace workspace;

    @Override
    public void init() {
        BVB = new BvbProject(this);

        Menu toolsMenu = TalosMain.Instance().UIStage().getToolsMenu();
        MenuItem newBvbProject = new MenuItem("New Skeletal Bridge");
        toolsMenu.addItem(newBvbProject);

        newBvbProject.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                TalosMain.Instance().ProjectController().newProject(BVB);
            }
        });

        buildUI();
    }

    private void buildUI() {
        workspace = new BvBWorkspace();
    }

    @Override
    public void initUIContent() {
        TalosMain.Instance().UIStage().swapToAddonContent(null, workspace, null);
        TalosMain.Instance().disableNodeStage();
    }

    @Override
    public boolean projectFileDrop(FileHandle handle) {
        if(handle.extension().equals("bvb")) {
            TalosMain.Instance().ProjectController().setProject(getProjectType());
            TalosMain.Instance().ProjectController().loadProject(handle);

            return true;
        }

        if(handle.extension().equals(".json")) {
            // cool let's load skeletal animation

            return true;
        }

        if(handle.extension().equals(".p")) {
            // adding particle effect? I can do that

            return true;
        }

        return false;
    }

    @Override
    public IProject getProjectType() {
        return BVB;
    }

    @Override
    public void announceLocalSettings(SettingsDialog settingsDialog) {
        settingsDialog.addPathSetting("BVB Spine JSON Default Path", "bvbSpineJsonPath");
        settingsDialog.addPathSetting("BVB .p Default Path", "bvbParticlePath");
        settingsDialog.addPathSetting("BVB Spine Atlas lookup", "bvbSpineAtlasPath");
    }
}
