package com.talosvfx.talos.editor.widgets.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kotcrab.vis.ui.widget.tabbedpane.Tab;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.project.IProject;

public class FileTab extends Tab {

    public String fileName;

    private IProject projectType;
    private boolean unworthy = false;

    public FileTab(String fileName, IProject projectType) {
        super(true, true);
        this.projectType = projectType;
        this.fileName = fileName;
    }
    @Override
    public String getTabTitle() {
        return fileName;
    }


    @Override
    public int hashCode() {
        return fileName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(fileName == null || obj == null) return false;

        return fileName.equals(((FileTab)obj).fileName);
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

    public IProject getProjectType() {
        return projectType;
    }

    public void setUnworthy() {
        unworthy = true;
    }

    public void setWorthy() {
        unworthy = false;
    }

    public boolean isUnworthy() {
        return unworthy;
    }
}
