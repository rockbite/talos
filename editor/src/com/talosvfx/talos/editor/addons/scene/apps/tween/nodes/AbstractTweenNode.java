package com.talosvfx.talos.editor.addons.scene.apps.tween.nodes;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.talosvfx.talos.editor.nodes.NodeBoard;
import com.talosvfx.talos.editor.nodes.NodeWidget;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;

public class AbstractTweenNode extends NodeWidget {

    @Override
    public void init(Skin skin, NodeBoard nodeBoard) {
        super.init(skin, nodeBoard);

        headerTable.setBackground(ColorLibrary.obtainBackground(getSkin(), "node-header", ColorLibrary.BackgroundColor.GREEN));
    }

    @Override
    public void setSelected(boolean selected) {
        if (selected) {
            headerTable.setBackground(ColorLibrary.obtainBackground(getSkin(), "node-header", ColorLibrary.BackgroundColor.LIGHT_GREEN));
        } else {
            headerTable.setBackground(ColorLibrary.obtainBackground(getSkin(), "node-header", ColorLibrary.BackgroundColor.GREEN));
        }
    }
}
