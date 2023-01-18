package com.talosvfx.talos.editor.nodes.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.XmlReader;
import com.talosvfx.talos.editor.widgets.ui.common.LabelWithZoom;

public class LabelWidget extends AbstractWidget<Object> {

    private LabelWithZoom label;

    public LabelWidget() {
        super();
    }


    @Override
    public void init(Skin skin) {
        super.init(skin);

        label = new LabelWithZoom("", skin);
    }

    public void set(String text, int align) {
        content.clearChildren();

        label.setText(text);
        if (Align.isLeft(align)) {
            content.add(label).left().expandX().height(32);
        } else {
            content.add(label).right().expandX().height(32);
        }
    }

    @Override
    public void loadFromXML(XmlReader.Element element) {
        String text = element.getText();
        String portType = element.getAttribute("port", "input");

        int align = Align.left;
        if(portType != null && portType.equals("output")) {
            align = Align.right;
        }

        set(text, align);
    }

    @Override
    public Object getValue () {
        return null;
    }

    @Override
    public void read (Json json, JsonValue jsonValue) {

    }

    @Override
    public void write (Json json, String name) {

    }
}
