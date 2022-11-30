package com.talosvfx.talos.editor.nodes.widgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.*;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;

import java.awt.*;

public class SelectWidget extends AbstractWidget<String> {

    private SelectBox<String> selectBox;

    private ObjectMap<String, String> titleMap = new ObjectMap<>();
    private ObjectMap<String, String> keyMap = new ObjectMap<>();

    @Override
    public void init(Skin skin) {
        super.init(skin);

        selectBox = new SelectBox<String>(skin, "rounded" );

        content.add(selectBox).expandX().height(32).left().padLeft(-2).growX();

        selectBox.getStyle().background.setLeftWidth(8);
        selectBox.getStyle().backgroundOpen.setLeftWidth(8);
        selectBox.getStyle().backgroundOver.setLeftWidth(8);
        selectBox.getStyle().listStyle.background = ColorLibrary.obtainBackground(skin, "square-patch", ColorLibrary.BackgroundColor.BLACK_TRANSPARENT);
        selectBox.getStyle().listStyle.selection = ColorLibrary.obtainBackground(skin, "square-patch", ColorLibrary.BackgroundColor.LIGHT_BLUE);
        selectBox.getStyle().scrollStyle.background = null;

        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed (ChangeEvent changeEvent, Actor actor) {
               fireChangedEvent();
            }
        });
    }

    @Override
    public void loadFromXML(XmlReader.Element element) {
        Array<XmlReader.Element> options = element.getChildrenByName("option");
        Array<String> items = new Array<>();

        for (XmlReader.Element option: options) {
            String optionText = option.getText();
            items.add(optionText);
            titleMap.put(optionText, option.getAttribute("value"));
            keyMap.put(option.getAttribute("value"), optionText);
        }

        selectBox.setItems(items);
    }

    public void setOptions(String[] options) {
        for (String option : options) {
            titleMap.put(option, option);
            keyMap.put(option, option);
        }

        selectBox.setItems(options);
    }

    @Override
    public String getValue () {
        String title = selectBox.getSelected();
        String name = titleMap.get(title);

        return name;
    }

    public void setValue(String value) {
        selectBox.setSelected(keyMap.get(value));
    }

    @Override
    public void read (Json json, JsonValue jsonValue) {
        String val = jsonValue.asString();
        setValue(val);
    }

    @Override
    public void write (Json json, String name) {
        String value = titleMap.get(selectBox.getSelected());
        json.writeValue(name, value);
    }
}
