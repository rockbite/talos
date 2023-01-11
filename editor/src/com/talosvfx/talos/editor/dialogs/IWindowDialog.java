package com.talosvfx.talos.editor.dialogs;

import com.badlogic.gdx.scenes.scene2d.ui.Table;

public interface IWindowDialog {

    String getTitle();
    Table getContent();

    int getDialogWidth();
    int getDialogHeight();
}
