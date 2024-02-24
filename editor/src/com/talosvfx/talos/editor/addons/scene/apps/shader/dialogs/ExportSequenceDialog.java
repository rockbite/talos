package com.talosvfx.talos.editor.addons.scene.apps.shader.dialogs;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.talosvfx.talos.editor.addons.scene.apps.shader.workspace.ShaderNodeStage;
import com.talosvfx.talos.editor.filesystem.FileChooserListener;
import com.talosvfx.talos.editor.filesystem.FileSystemInteraction;

public class ExportSequenceDialog extends VisWindow {

    private final ShaderNodeStage nodeStage;
    private TextField fileName;
    private TextField inputPathField;
    private TextField widthField;
    private TextField heightFIeld;
    private TextField durationField;
    private TextField fpsField;


    public ExportSequenceDialog(ShaderNodeStage nodeStage) {
        super("Export Sequence");

        this.nodeStage = nodeStage;

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

    private Table makeRow(String labelText, Actor field) {
        Table table = new Table();

        table.add(new Label(labelText, getSkin())).width(220);
        table.add(field).padLeft(13).grow().left().expandX();

        return table;
    }

    private void initContent() {
        fileName = new TextField("sequence", getSkin());
        Table fileNameRow = makeRow("Name prefix", fileName);
        add(fileNameRow).pad(5).left().expandX().padTop(10);
        row();

        Table pathBrowseField = new Table();
        inputPathField = new TextField("", getSkin());
        pathBrowseField.add(inputPathField).width(200);
        TextButton browseInputBtn = new TextButton("Browse", getSkin());
        pathBrowseField.add(browseInputBtn).padLeft(3);
        Table pathRow = makeRow("Output Directory", pathBrowseField);
        add(pathRow).pad(5).left().expandX().padTop(10);
        row();

        widthField = new TextField("256", getSkin());
        Table widthRow = makeRow("Width: ", widthField);
        add(widthRow).pad(5).left().expandX().padTop(10);
        row();

        heightFIeld = new TextField("256", getSkin());
        Table heightRow = makeRow("Height: ", heightFIeld);
        add(heightRow).pad(5).left().expandX().padTop(10);
        row();

        durationField = new TextField("1", getSkin());
        Table durationRow = makeRow("Duration: ", durationField);
        add(durationRow).pad(5).left().expandX().padTop(10);
        row();

        fpsField = new TextField("40", getSkin());
        Table fpsRow = makeRow("FPS: ", fpsField);
        add(fpsRow).pad(5).left().expandX().padTop(10);
        row();

        browseInputBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);

                FileSystemInteraction.instance().showFolderChooser(new FileChooserListener() {
                    @Override
                    public void selected (Array<FileHandle> files) {
                        inputPathField.setText(files.get(0).path());
                    }
                });
            }
        });

        TextButton saveButton = new TextButton("Save", getSkin());
        add(saveButton).right().padRight(5);
        row();

        saveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                save();
            }
        });
    }

    private void save() {
        nodeStage.exportSequence(fileName.getText(), inputPathField.getText(), Integer.parseInt(widthField.getText()), Integer.parseInt(heightFIeld.getText()), Float.parseFloat(durationField.getText()), Integer.parseInt(fpsField.getText()));

        close();
    }
}
