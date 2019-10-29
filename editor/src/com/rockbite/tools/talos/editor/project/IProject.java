package com.rockbite.tools.talos.editor.project;

import com.badlogic.gdx.files.FileHandle;

public interface IProject {
    void loadProject(String data);

    String getProjectString();

    void resetToNew();

    String getExtension();

    String getProjectNameTemplate();

    void initUIContent();

    FileHandle findFileInDefaultPaths(String fileName);
}
