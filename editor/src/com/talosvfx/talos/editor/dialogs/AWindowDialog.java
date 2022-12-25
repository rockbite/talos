package com.talosvfx.talos.editor.dialogs;

import com.badlogic.gdx.scenes.scene2d.ui.Table;

public abstract class AWindowDialog implements IWindowDialog {

    private final Table content;

    public AWindowDialog() {
        content = build();
    }

    public abstract Table build();

    public abstract String getTitle();

    @Override
    public Table getContent() {
        return content;
    }

    public void show() {

    }

    public void close() {

    }

    @Override
    public int getDialogWidth() {
        return (int) content.getWidth();
    }

    @Override
    public int getDialogHeight() {
        return (int) content.getHeight();
    }
}
