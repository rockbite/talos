package com.rockbite.tools.talos.editor.widgets.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kotcrab.vis.ui.widget.tabbedpane.Tab;
import com.rockbite.tools.talos.TalosMain;

public class FileTab extends Tab {

    public String fileName;

    public FileTab(String fileName) {
        super(true, true);

        this.fileName = fileName;
    }
    @Override
    public String getTabTitle() {
        return fileName;
    }

    @Override
    public boolean save() {
        if(isSavable()) {
            TalosMain.Instance().UIStage().saveProjectAction();
        }

        return false;
    }

    @Override
    public Table getContentTable() {
        return null;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
