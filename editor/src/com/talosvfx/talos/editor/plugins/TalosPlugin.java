package com.talosvfx.talos.editor.plugins;

public abstract class TalosPlugin<T extends TalosPluginProvider> {


    private final T provider;

    //To be injected
    public TalosPlugin (T provider) {
        this.provider = provider;
    }

    public void onPluginProviderInitialized () {
    }

    public T getProvider () {
        return provider;
    }


}
