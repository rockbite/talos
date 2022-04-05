package com.talosvfx.talos.editor.addons.shader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuBar;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.IAddon;
import com.talosvfx.talos.editor.addons.shader.dialogs.ExportSequenceDialog;
import com.talosvfx.talos.editor.addons.shader.workspace.ShaderNodeStage;
import com.talosvfx.talos.editor.filesystem.FileChooserListener;
import com.talosvfx.talos.editor.nodes.DynamicNodeStage;
import com.talosvfx.talos.editor.dialogs.SettingsDialog;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.events.AssetFileDroppedEvent;
import com.talosvfx.talos.editor.project.IProject;

public class ShaderAddon implements IAddon {

    public static ShaderProject SHADER_PROJECT;

    public DynamicNodeStage nodeStage;

    ExportSequenceDialog exportSequenceDialog;

    @Override
    public void init () {
        SHADER_PROJECT = new ShaderProject(this);

        nodeStage = new ShaderNodeStage(TalosMain.Instance().UIStage().getSkin());
        nodeStage.init();

        buildUI();
    }

    private void buildUI () {
        exportSequenceDialog = new ExportSequenceDialog((ShaderNodeStage)nodeStage);
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
        Menu menu = new Menu("Shader Graph");

        MenuItem newFile = new MenuItem("New Shader");
        menu.addItem(newFile);
        MenuItem openFile = new MenuItem("Open Shader");
        menu.addItem(openFile);
        MenuItem exportRaw = new MenuItem("Export RAW");
        menu.addItem(exportRaw);

        MenuItem exportPNG = new MenuItem("Export PNG");
        menu.addItem(exportPNG);

        MenuItem exportSequence = new MenuItem("Export Sequence");
        menu.addItem(exportSequence);

        newFile.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                TalosMain.Instance().ProjectController().newProject(SHADER_PROJECT);
            }
        });

        openFile.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                TalosMain.Instance().UIStage().openProjectAction(SHADER_PROJECT);
            }
        });
        exportRaw.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                // export RAW logic

                TalosMain.Instance().UIStage().showSaveFileChooser(".frag", new FileChooserListener() {
                    @Override
                    public void selected (Array<FileHandle> files) {
                        String fragShader = ((ShaderNodeStage)(nodeStage)).getFragShader();

                        FileHandle file = files.get(0);

                        file.writeString(fragShader, false);
                    }
                });
            }
        });

        exportPNG.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                // export RAW logic

                TalosMain.Instance().UIStage().showSaveFileChooser(".png", new FileChooserListener() {
                    @Override
                    public void selected (Array<FileHandle> files) {
                        FileHandle file = files.get(0);
                        Pixmap pixmap = ((ShaderNodeStage)(nodeStage)).exportPixmap();
                        if(pixmap != null) {
//                            PixmapIO.writePNG(file, pixmap);
                            pixmap.dispose();
                        }
                    }
                });
            }
        });

        exportSequence.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                TalosMain.Instance().UIStage().getStage().addActor(exportSequenceDialog.fadeIn());
            }
        });

        menuBar.addMenu(menu);
    }
}
