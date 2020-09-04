package com.talosvfx.talos.editor.nodes.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.XmlReader;

public class CheckBoxWidget extends AbstractWidget<Boolean> {

    private CheckBox checkBox;

    @Override
    public void init(Skin skin) {
        super.init(skin);

        checkBox = new CheckBox("checkbox", skin, "rounded-checkbox");
        content.add(checkBox).left().expandX().height(32).padLeft(-5);
    }

    @Override
    public void loadFromXML(XmlReader.Element element) {
        checkBox.setText(element.getText());
    }

    @Override
    public Boolean getValue () {
        return checkBox.isChecked();
    }


    @Override
    public void read (Json json, JsonValue jsonValue) {
        checkBox.setChecked(jsonValue.asBoolean());
    }

    @Override
    public void write (Json json, String name) {
        json.writeValue(name, checkBox.isChecked());
    }
}
