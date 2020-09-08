package com.talosvfx.talos.plugins.nodes;

import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.nodes.NodeWidget;

public class TestNodeWidget extends NodeWidget {

    @Override
    public void constructNode (XmlReader.Element module) {
        System.out.println("Constructed test custom node widget");
        super.constructNode(module);
    }
}
