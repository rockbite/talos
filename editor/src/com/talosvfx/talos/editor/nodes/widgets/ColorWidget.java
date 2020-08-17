package com.talosvfx.talos.editor.nodes.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;

public class ColorWidget extends AbstractWidget {

    @Override
    public void init(Skin skin) {
        super.init(skin);

        Label label = new Label("Color", skin);

        Table colorButton = new Table();
        colorButton.setBackground(skin.newDrawable(ColorLibrary.SHAPE_SQUIRCLE));

        content.add(label).left().expandX().height(32);
        content.add(colorButton).right().expandX().height(32).width(96);
        colorButton.setColor(Color.RED);
    }

    @Override
    public void loadFromXML(XmlReader.Element element) {

    }


}
