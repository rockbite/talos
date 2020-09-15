package com.talosvfx.talos.editor.addons.treedata.nodes;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.talosvfx.talos.editor.nodes.NodeWidget;
import com.talosvfx.talos.editor.nodes.PluginNodeWidget;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.plugins.TalosPluginProvider;

public class BasicDataNode<T extends TalosPluginProvider> extends PluginNodeWidget<T> implements Notifications.Observer {

    public BasicDataNode (Skin skin) {
        super(skin);
    }
}
