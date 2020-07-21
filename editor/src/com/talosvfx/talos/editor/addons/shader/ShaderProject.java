package com.talosvfx.talos.editor.addons.shader;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.project.IProject;

public class ShaderProject implements IProject {

    ShaderAddon shaderAddon;

    public ShaderProject(ShaderAddon shaderAddon) {
        this.shaderAddon = shaderAddon;
    }

    @Override
    public void loadProject (String data) {

    }

    @Override
    public String getProjectString () {
        return "{}";
    }

    @Override
    public void resetToNew () {

    }

    @Override
    public String getExtension () {
        return ".shader";
    }

    @Override
    public String getExportExtension () {
        return ".frag";
    }

    @Override
    public String getProjectNameTemplate () {
        return "Shader";
    }

    @Override
    public void initUIContent () {
        shaderAddon.initUIContent();
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
