package com.talosvfx.talos.editor.project;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

public interface IProject {
    void loadProject(String data);

    String getProjectString();

    void resetToNew();

    String getExtension();

    String getExportExtension();

    String getProjectNameTemplate();

    void initUIContent();

    FileHandle findFileInDefaultPaths(String fileName);

    Array<String> getSavedResourcePaths ();

    String exportProject();

}
