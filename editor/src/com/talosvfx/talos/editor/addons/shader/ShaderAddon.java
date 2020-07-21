package com.talosvfx.talos.editor.addons.shader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.XmlReader;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuBar;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.IAddon;
import com.talosvfx.talos.editor.addons.shader.workspace.ShaderNodeStage;
import com.talosvfx.talos.editor.nodes.DynamicNodeStage;
import com.talosvfx.talos.editor.dialogs.SettingsDialog;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.events.AssetFileDroppedEvent;
import com.talosvfx.talos.editor.project.IProject;

public class ShaderAddon implements IAddon {

    public static ShaderProject SHADER_PROJECT;

    public DynamicNodeStage nodeStage;

    private ShaderBuilder shaderBuilder;

    private ShaderTimeline timeline;

    @Override
    public void init () {
        SHADER_PROJECT = new ShaderProject(this);

        nodeStage = new ShaderNodeStage(TalosMain.Instance().UIStage().getSkin());
        nodeStage.init();

        buildUI();
    }

    private void buildUI () {
        timeline = new ShaderTimeline(TalosMain.Instance().UIStage().getSkin());
    }

    @Override
    public void initUIContent () {
        TalosMain.Instance().UIStage().swapToAddonContent(null, null, timeline);
        TalosMain.Instance().setThirdPartyStage(nodeStage);

        // now need to disable some menu tabs
        TalosMain.Instance().UIStage().Menu().disableTalosSpecific();
    }

    @Override
    public boolean projectFileDrop (FileHandle handle) {
        // a shader project should be loaded, but any other files go straight to modules

        IProject currProjectType = TalosMain.Instance().ProjectController().getProject();

        if(currProjectType == SHADER_PROJECT) {
            AssetFileDroppedEvent event = Notifications.obtainEvent(AssetFileDroppedEvent.class);
            event.setFileHandle(handle);
            event.setScreenPos(Gdx.input.getX(), Gdx.input.getY());
            Notifications.fireEvent(event);
        }

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
