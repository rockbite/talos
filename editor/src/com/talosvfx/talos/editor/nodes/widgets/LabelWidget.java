package com.talosvfx.talos.editor.nodes.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.XmlReader;

public class LabelWidget extends AbstractWidget<Object> {

    private Label label;

    public LabelWidget() {
        super();
    }


    @Override
    public void init(Skin skin) {
        super.init(skin);

        label = new Label("", skin);
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
}
