package com.talosvfx.talos.editor.addons.scene.apps.tween.nodes;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.addons.scene.apps.tween.TweenStage;
import com.talosvfx.talos.editor.nodes.widgets.ButtonWidget;
import com.talosvfx.talos.editor.nodes.widgets.TextValueWidget;

public class TweenNode extends AbstractTweenNode {

    @Override
    protected void addAdditionalContent(Table contentTable) {

    }

    @Override
    public void constructNode(XmlReader.Element module) {
        super.constructNode(module);

        // add some additional widget logic here, if ay
    }

    public String getTweenTitle() {
        TextValueWidget titleText = (TextValueWidget) getWidget("title");
        return titleText.getValue();
    }
}
