package com.rockbite.tools.talos.editor.project;

import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.addons.bvb.BvBAddon;

public class BvbProject implements IProject {

    BvBAddon bvBAddon;

    public BvbProject(BvBAddon addon) {
        bvBAddon = addon;
    }

    @Override
    public void loadProject(String data) {

    }

    @Override
    public String getProjectString() {
        return null;
    }

    @Override
    public void resetToNew() {

    }

    @Override
    public String getExtension() {
        return ".bvb";
    }

    @Override
    public String getProjectNameTemplate() {
        return "skeleton";
    }

    @Override
    public void initUIContent() {
        bvBAddon.initUIContent();
    }
}
