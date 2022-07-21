package com.talosvfx.talos.editor.addons.scene.apps;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class TweenEditor extends AEditorApp<FileHandle> {

    private String title;

    public TweenEditor(FileHandle twFileHandle) {
        super(twFileHandle);
        identifier = twFileHandle.path();
        title = twFileHandle.name();
        initContent();
    }

    @Override
    public void initContent() {
        content = new Table();
    }

    @Override
    public String getTitle() {
        return title;
    }
}
