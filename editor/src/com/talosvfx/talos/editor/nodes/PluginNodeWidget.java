package com.talosvfx.talos.editor.nodes;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.*;
import com.talosvfx.talos.editor.plugins.TalosPluginProvider;

public abstract class PluginNodeWidget<T extends TalosPluginProvider> extends NodeWidget implements Json.Serializable {

    protected T talosPluginProvider;

    public PluginNodeWidget (Skin skin) {
        super(skin);
    }

    public void injectProvder (T talosPluginProvider) {
        this.talosPluginProvider = talosPluginProvider;
    }
}
