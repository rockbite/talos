package com.talosvfx.talos.editor.addons.scene.dialogs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.socket.SocketServer;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IntPropertyWidget;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;

import java.io.File;
import java.net.Socket;
import com.talosvfx.talos.runtime.utils.Supplier;

public class SettingsDialog extends VisWindow {

    private Table exportInlineContainer;
    private TextField exportScriptPathField;
    private SelectBox<String> selectBox;

    private int serverPort = SocketServer.SERVER_PORT;

    private IntPropertyWidget serverPortWidget;

    public SettingsDialog() {
        super("Project Settings");

        setCenterOnAdd(true);
        setModal(true);
        setResizable(true);
        setMovable(true);
        addCloseButton();
        closeOnEscape();

        initContent();

        pack();
        invalidate();

        centerWindow();
    }

    public void addScriptPathSetting(Table container) {
        Table inputTable = new Table();

        String sceneEditorExportScriptPath = TalosMain.Instance().Prefs().getString("sceneEditorExportScriptPath", null);

        inputTable.add(new Label("BuildScript location (JS)", getSkin())).width(180);
        exportScriptPathField = new TextField(sceneEditorExportScriptPath, getSkin());
        inputTable.add(exportScriptPathField).padLeft(13).width(130);
        TextButton browseInputBtn = new TextButton("Browse", getSkin());
        inputTable.add(browseInputBtn).padLeft(3);

        FileChooser fileChooser = new FileChooser(FileChooser.Mode.SAVE);
        fileChooser.setBackground(getSkin().getDrawable("window-noborder"));

        browseInputBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                fileChooser.setMode(FileChooser.Mode.SAVE);
                fileChooser.setMultiSelectionEnabled(false);
                fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);

                String currentProjectPath = TalosMain.Instance().ProjectController().getCurrentProjectPath();

                String sceneEditorExportScriptPath = TalosMain.Instance().Prefs().getString("sceneEditorExportScriptPath", null);

                if(sceneEditorExportScriptPath != null) {
                    FileHandle path = Gdx.files.absolute(sceneEditorExportScriptPath);
                    fileChooser.setDirectory(path.parent().file());
                    fileChooser.setDefaultFileName(path.name());
                    exportScriptPathField.setText(path.path());
                } else {
                    fileChooser.setDirectory(Gdx.files.absolute(currentProjectPath).parent().file());
                    fileChooser.setDefaultFileName("buildscript.js");
                }

                fileChooser.setListener(new FileChooserAdapter() {
                    @Override
                    public void selected (Array<FileHandle> files) {
                        FileHandle file = files.first();
                        exportScriptPathField.setText(file.path());
                    }
                });

                getStage().addActor(fileChooser.fadeIn());
            }
        });

        container.add(inputTable).pad(10).expand().growX().left();
        container.row();
    }

    public void initData() {
        String exportType = TalosMain.Instance().Prefs().getString("exportType", "Default");
        selectBox.setSelected(exportType);
    }


    private void initContent() {

        add(new Label("Export Settings", getSkin())).left().expandX().pad(10);
        row();

        selectBox = new SelectBox<>( getSkin());
        String[] labels = new String[] {"Default", "Custom Script"};
        selectBox.setItems(labels);
        add(selectBox).width(400).pad(10);

        row();

        exportInlineContainer = new Table();
        add(exportInlineContainer);
        row();

        addServerPortSettings();
        row();

        TextButton saveButton = new TextButton("Save", getSkin());
        add(saveButton).right().padRight(5).padTop(10);
        row();

        saveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                save();
            }
        });

        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selected = selectBox.getSelected();
                if(selected.equals("Default")) {
                    exportInlineContainer.clear();
                } else {
                    exportInlineContainer.clear();
                    addScriptPathSetting(exportInlineContainer);
                    pack();
                    invalidate();
                }
            }
        });
    }

    @Override
    public VisWindow fadeIn (float fadeTime) {
        serverPort = SocketServer.SERVER_PORT;
        serverPortWidget.setValue(serverPort);
        return super.fadeIn(fadeTime);
    }

    private void addServerPortSettings () {
        serverPortWidget = new IntPropertyWidget("Server Port", new Supplier<Integer>() {
            @Override
            public Integer get () {
                return serverPort;
            }
        }, new PropertyWidget.ValueChanged<Integer>() {
            @Override
            public void report (Integer value) {
                serverPort = value;
            }
        }, null);

        add(serverPortWidget).pad(5).growX();
    }


    private void save() {
        String selected = selectBox.getSelected();

        TalosMain.Instance().Prefs().putString("exportType", selected);
        TalosMain.Instance().Prefs().putInteger("serverPort", serverPort);

        if(selected.equals("Default")) {
            TalosMain.Instance().Prefs().remove("sceneEditorExportScriptPath");
        } else {
            String path = exportScriptPathField.getText();
            if(path.length() > 0) {

                FileHandle handle = Gdx.files.absolute(path);
                if(!(new File(path)).isAbsolute()) {
                    handle = Gdx.files.absolute(Gdx.files.absolute(TalosMain.Instance().ProjectController().getCurrentProjectPath()).parent().path() + File.separator + path);
                }

                if (!handle.exists()) {
                    try {
                        handle.write(false);
                        TalosMain.Instance().Prefs().putString("sceneEditorExportScriptPath", path);
                    } catch (Exception e) {

                    }
                } else {
                    TalosMain.Instance().Prefs().putString("sceneEditorExportScriptPath", path);
                }
            }
        }

        if (serverPort != SocketServer.SERVER_PORT) {
            SocketServer.SERVER_PORT = serverPort;
            SocketServer.restartServer();
        }

        TalosMain.Instance().Prefs().flush();


        close();
    }
}
