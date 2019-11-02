package com.rockbite.tools.talos.editor.addons.bvb;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;

public class PropertiesPanel extends Window {

    public PropertiesPanel(Skin skin) {
        super("Global Properties", skin);
        setBackground(skin.getDrawable("panel"));

        padLeft(5);
        padTop(25);

        setModal(false);
        setMovable(false);



        // content here for now
        Label label = new Label("camera mode: ", getSkin());
        TextField textField = new TextField("", getSkin(), "dark");

        add(label);
        add(textField);
        row();
    }
}
