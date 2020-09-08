package com.talosvfx.talos.plugins;

import com.talosvfx.talos.editor.plugins.TalosPluginProvider;

public class InternalPluginProvider extends TalosPluginProvider {

    @Override
    public void initialize () {
        System.out.println("Internal Plugin provider initialized");
    }

}
