package com.talosvfx.talos.plugins;

import com.talosvfx.talos.editor.plugins.TalosPlugin;

public class TestPlugin extends TalosPlugin<InternalPluginProvider> {

    public TestPlugin (InternalPluginProvider provider) {
        super(provider);
    }

    @Override
    public void onPluginProviderInitialized () {
        System.out.println("Test plugin's PluginProvider has been initialized");

        System.out.println("Im new code 5!");
    }
}
