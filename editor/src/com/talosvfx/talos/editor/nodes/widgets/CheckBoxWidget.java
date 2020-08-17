package com.talosvfx.talos.editor.nodes.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.XmlReader;

public class CheckBoxWidget extends AbstractWidget {

    @Override
    public void init(Skin skin) {
        super.init(skin);

        CheckBox checkBox = new CheckBox("Clamp Output", skin, "rounded-checkbox");
        content.add(checkBox).left().expandX().height(32).padLeft(-5);
    }

    @Override
    public void loadFromXML(XmlReader.Element element) {

    }
}
