package com.talosvfx.talos.editor.addons.treedata;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuBar;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.IAddon;
import com.talosvfx.talos.editor.addons.shader.ShaderProject;
import com.talosvfx.talos.editor.addons.shader.workspace.ShaderNodeStage;
import com.talosvfx.talos.editor.addons.treedata.workspace.TreeDataNodeStage;
import com.talosvfx.talos.editor.dialogs.SettingsDialog;
import com.talosvfx.talos.editor.nodes.DynamicNodeStage;
import com.talosvfx.talos.editor.project.IProject;

public class TreeDataAddon implements IAddon {

    public static TreeDataProject TD_PROJECT;
    public DynamicNodeStage nodeStage;

    @Override
    public void init () {
        TD_PROJECT = new TreeDataProject(this);

        nodeStage = new TreeDataNodeStage(TalosMain.Instance().UIStage().getSkin());
        nodeStage.init();

        buildUI();
    }


    private void buildUI () {

    }

    @Override
    public void initUIContent () {
        TalosMain.Instance().UIStage().swapToAddonContent(null, null, null);
        TalosMain.Instance().setThirdPartyStage(nodeStage);

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
        Menu menu = new Menu("Data Builder");

        MenuItem loadConfig = new MenuItem("Load Workspace Config");
        menu.addItem(loadConfig);
        MenuItem newFile = new MenuItem("New Data Project");
        menu.addItem(newFile);
        MenuItem openFile = new MenuItem("Open Data Project");
        menu.addItem(openFile);

        loadConfig.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                TalosMain.Instance().UIStage().showFileChooser("xml", new FileChooserAdapter() {
                    @Override
                    public void selected (Array<FileHandle> files) {
                        super.selected(files);
                        FileHandle file = files.first();
                        XmlReader xmlReader = new XmlReader();
                        XmlReader.Element root = xmlReader.parse(file);
                        nodeStage.setData(root);
                        nodeStage.init();
                        TalosMain.Instance().getDynamicLibrary().loadWorkspaceLibraries(root);
                    }
                });
            }
        });


        newFile.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                TalosMain.Instance().ProjectController().newProject(TD_PROJECT);
            }
        });

        openFile.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                TalosMain.Instance().UIStage().openProjectAction(TD_PROJECT);
            }
        });

        menuBar.addMenu(menu);
    }
}
