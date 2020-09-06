package com.talosvfx.talos.editor.addons.treedata;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.project.IProject;

public class TreeDataProject implements IProject {

    private final TreeDataAddon treeDataAddon;

    public TreeDataProject (TreeDataAddon treeDataAddon) {
        this.treeDataAddon = treeDataAddon;
    }

    @Override
    public void loadProject (String data) {

    }

    @Override
    public String getProjectString () {
        return null;
    }

    @Override
    public void resetToNew () {

    }

    @Override
    public String getExtension () {
        return ".dcg"; // Dynamic Content Graph
    }

    @Override
    public String getExportExtension () {
        return ".xml";
    }

    @Override
    public String getProjectNameTemplate () {
        return "ContentFile";
    }

    @Override
    public void initUIContent () {
        treeDataAddon.initUIContent();
    }

    @Override
    public FileHandle findFileInDefaultPaths (String fileName) {
        return null;
    }

    @Override
    public Array<String> getSavedResourcePaths () {
        return null;
    }

    @Override
    public String exportProject () {
        return null;
    }
}
