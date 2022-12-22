package com.talosvfx.talos.editor.widgets.ui;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kotcrab.vis.ui.widget.tabbedpane.Tab;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.project.IProject;

public class FileTab extends Tab {

    public FileHandle projectFileHandle;
    private IProject projectType;
    private boolean unworthy = false;

    public FileTab(FileHandle projectFileHandle, IProject projectType) {
        super(true, true);
        this.projectFileHandle = projectFileHandle;
        this.projectType = projectType;
    }

    @Override
    public String getTabTitle() {
        return projectFileHandle.name();
    }


    @Override
    public int hashCode() {
        return projectFileHandle.name().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(projectFileHandle.name() == null || obj == null) return false;

        return projectFileHandle.name().equals(((FileTab)obj).projectFileHandle.name());
    }

    @Override
    public boolean save() {
        if(isSavable()) {
            //TalosMain.Instance().UIStage().saveProjectAction();
        }

        return false;
    }

    @Override
    public Table getContentTable() {
        return null;
    }

    public String getFileName() {
        return projectFileHandle.name();
    }

    public FileHandle getProjectFileHandle() {
        return projectFileHandle;
    }

    public void setProjectFileHandle (FileHandle projectFileHandle) {
        this.projectFileHandle = projectFileHandle;
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
