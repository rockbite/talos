package com.rockbite.tools.talos.editor.project;

public interface IProject {
    void loadProject(String data);

    String getProjectString();

    void resetToNew();
}
