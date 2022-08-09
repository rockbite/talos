package com.talosvfx.talos.editor.dialogs;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.talosvfx.talos.TalosMain;

public class NewProjectDialog extends VisWindow {

    FileChooser fileChooser;
    private TextField projectNameField;
    private TextField parentPathField;
    private NewProjectListener listener;

    public NewProjectDialog () {
        super("New Project");

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

        fileChooser = new FileChooser(FileChooser.Mode.OPEN);
        fileChooser.setBackground(getSkin().getDrawable("window-noborder"));
    }

    public interface NewProjectListener {
        void create(String path, String name);
    }

    public static void show (String typeName, String projectName, NewProjectListener listener) {
        NewProjectDialog dialog = TalosMain.Instance().UIStage().newProjectDialog;
        dialog.setData(typeName, projectName, listener);
        TalosMain.Instance().UIStage().openDialog(dialog);
    }

    private void setData (String typeName, String projectName, NewProjectListener listener) {
        this.listener = listener;
        getTitleLabel().setText("New " + typeName + " Project");
        projectNameField.setText(projectName);
        parentPathField.setText(TalosMain.Instance().Prefs().getString("sceneEditorProjectsPath"));
    }

    private void initContent () {
        addProjectNameRow();
        addPathRow();

        TextButton saveButton = new TextButton("Save", getSkin());
        add(saveButton).right().padRight(5);
        row();

        saveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                if (parentPathField.getText().isEmpty()) {
                    showFolderSelect();
                } else {
                    save();
                }
            }
        });
    }

    private void save () {
        if(listener != null) {
            listener.create(parentPathField.getText(), projectNameField.getText());
        }

        remove();
    }

    private void addProjectNameRow () {

        Table inputTable = new Table();

        inputTable.add(new Label("Project Name: ", getSkin())).width(220);
        projectNameField = new TextField("", getSkin());
        inputTable.add(projectNameField).padLeft(13).growX();

        add(inputTable).pad(5).left().expandX().padTop(10).growX();
        row();
    }

    public void addPathRow() {
        Table inputTable = new Table();

        inputTable.add(new Label("Create project directory in: ", getSkin())).width(220);
        parentPathField = new TextField("", getSkin());
        parentPathField.setDisabled(true);
        inputTable.add(parentPathField).padLeft(13).width(270);
        TextButton browseInputBtn = new TextButton("Browse", getSkin());
        inputTable.add(browseInputBtn).padLeft(3);

        add(inputTable).pad(5).left().expandX().padTop(10);
        row();

        browseInputBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                showFolderSelect();
            }
        });
    }

    private void showFolderSelect() {
        fileChooser.setMode(FileChooser.Mode.OPEN);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setSelectionMode(FileChooser.SelectionMode.DIRECTORIES);

        fileChooser.setListener(new FileChooserAdapter() {
            @Override
            public void selected (Array<FileHandle> file) {
                parentPathField.setText(file.get(0).path());
            }
        });

        getStage().addActor(fileChooser.fadeIn());
    }
}
