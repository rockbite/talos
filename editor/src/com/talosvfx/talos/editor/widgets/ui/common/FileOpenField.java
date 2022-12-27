package com.talosvfx.talos.editor.widgets.ui.common;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.talosvfx.talos.editor.filesystem.FileChooserListener;
import com.talosvfx.talos.editor.filesystem.FileSystemInteraction;
import com.talosvfx.talos.editor.project2.SharedResources;
import lombok.Getter;

public class FileOpenField extends Table {

    private final VisTextField projectDirectory;
    @Getter
    private final Table inputContainer;

    public FileOpenField() {
        projectDirectory = new VisTextField(Gdx.files.absolute(System.getProperty("user.home")).file().getAbsolutePath());
        RoundedFlatButton selectDirectory = new RoundedFlatButton(); selectDirectory.makeRight("Open");

        inputContainer = new Table();
        inputContainer.setBackground(ColorLibrary.obtainBackground(SharedResources.skin, ColorLibrary.SHAPE_SQUIRCLE_LEFT, ColorLibrary.BackgroundColor.DARK_GRAY));

        projectDirectory.getStyle().background = null;
        projectDirectory.getStyle().focusedBackground = null;
        projectDirectory.getStyle().backgroundOver = null;
        projectDirectory.getStyle().focusBorder = null;

        inputContainer.add(projectDirectory).grow().padLeft(10);

        add(inputContainer).height(30).growX();
        add(selectDirectory);

        selectDirectory.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                FileSystemInteraction.instance().showFolderChooser(new FileChooserListener() {
                    @Override
                    public void selected(Array<FileHandle> files) {
                        projectDirectory.setText(files.first().path());
                        fireChangedEvent();
                    }
                });
            }
        });
    }

    public String getPath() {
        return projectDirectory.getText();
    }

    protected boolean fireChangedEvent() {
        ChangeListener.ChangeEvent changeEvent = Pools.obtain(ChangeListener.ChangeEvent.class);

        boolean var2 = false;
        try {
            var2 = fire(changeEvent);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Pools.free(changeEvent);
        }

        return var2;
    }

    public void setPath(String path) {
        projectDirectory.setText(path);
    }
}
