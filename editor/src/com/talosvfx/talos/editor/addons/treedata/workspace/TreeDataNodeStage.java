package com.talosvfx.talos.editor.addons.treedata.workspace;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.nodes.DynamicNodeStage;
import com.talosvfx.talos.editor.notifications.Notifications;

public class TreeDataNodeStage extends DynamicNodeStage implements Notifications.Observer {

    public TreeDataNodeStage (Skin skin) {
        super(skin);
    }

    @Override
    protected XmlReader.Element loadData () {
        return null;
    }
}
