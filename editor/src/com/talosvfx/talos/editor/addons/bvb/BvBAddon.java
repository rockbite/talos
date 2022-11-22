package com.talosvfx.talos.editor.addons.bvb;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuBar;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.IAddon;
import com.talosvfx.talos.editor.dialogs.SettingsDialog;
import com.talosvfx.talos.editor.project.FileTracker;
import com.talosvfx.talos.editor.project.IProject;

public class BvBAddon implements IAddon {

    public static BvbProject BVB;

    BvBWorkspace workspace;
    PropertyPanelContainer properties;
    ParticleList particleList;

    public FileTracker.Tracker spineTracker;
    public FileTracker.Tracker bvbBackgroundTracker;
    public FileTracker.Tracker particleTracker;

    @Override
    public void init() {
        BVB = new BvbProject(this);

        buildUI();

        spineTracker = handle -> workspace.setSkeleton(handle);

        particleTracker = handle -> workspace.updateParticle(handle);

        bvbBackgroundTracker = handle -> workspace.setBackgroundImage(handle);

    }

    @Override
    public void buildMenu(MenuBar menuBar) {
        Menu bvbMenu = new Menu("Animations");

        MenuItem newBvbProject = new MenuItem("New Project");
        bvbMenu.addItem(newBvbProject);
        MenuItem openBvbProject = new MenuItem("Open Project");
        bvbMenu.addItem(openBvbProject);
        // TODO: add other menu items here

        newBvbProject.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                TalosMain.Instance().ProjectController().newProject(BVB);
            }
        });

        openBvbProject.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                TalosMain.Instance().UIStage().openProjectAction(BVB);
            }
        });

        menuBar.addMenu(bvbMenu);
    }

    @Override
    public void dispose () {

    }

    private void buildUI() {
        properties = new PropertyPanelContainer(TalosMain.Instance().UIStage().getSkin());
        workspace = BvBWorkspace.getInstance();
        workspace.setBvBAddon(this);
        particleList = new ParticleList(workspace, TalosMain.Instance().UIStage().getSkin());
    }

    @Override
    public void initUIContent() {
        TalosMain.Instance().UIStage().swapToAddonContent(properties, workspace, particleList);
        TalosMain.Instance().disableNodeStage();

        // now need to disable some menu tabs
        TalosMain.Instance().UIStage().Menu().disableTalosSpecific();

        TalosMain.Instance().UIStage().getStage().setKeyboardFocus(workspace);
    }

    @Override
    public boolean projectFileDrop(FileHandle handle) {
        if(handle.extension().equals("bvb")) {
            TalosMain.Instance().ProjectController().setProject(getProjectType());
            TalosMain.Instance().ProjectController().loadProject(handle);

            return true;
        }

        IProject currProjectType = TalosMain.Instance().ProjectController().getProject();

        if(currProjectType == BVB) {

            if (handle.extension().equals("json")) {
                // cool let's load skeletal animation
                workspace.setSkeleton(handle);
                TalosMain.Instance().FileTracker().trackFile(handle, spineTracker);

                return true;
            }

            if (handle.extension().equals("p")) {
                // adding particle effect? I can do that
                workspace.addParticle(handle);
                TalosMain.Instance().FileTracker().trackFile(handle, particleTracker);

                return true;
            }

            if (handle.extension().equals("png")) {
                // let's load some background weeeee
                workspace.addBackgroundImage(handle);
                TalosMain.Instance().FileTracker().trackFile(handle, bvbBackgroundTracker);
            }
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

    public BvBWorkspace getWorkspace() {
        return  workspace;
    }

    public ParticleList getTimeline () {
        return particleList;
    }
}
