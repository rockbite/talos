package com.talosvfx.talos.editor.nodes.widgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.*;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;
import com.talosvfx.talos.editor.widgets.ui.common.zoomWidgets.SelectBoxWithZoom;


public class SelectWidget extends AbstractWidget<String> {

    private SelectBoxWithZoom<String> selectBox;

    private ObjectMap<String, String> titleMap = new ObjectMap<>();
    private ObjectMap<String, String> keyMap = new ObjectMap<>();

    private boolean lockEvents = false;

    @Override
    public void init(Skin skin) {
        super.init(skin);

        selectBox = new SelectBoxWithZoom<String>(skin, "rounded" );

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
                if(!lockEvents) {
                    fireChangedEvent();
                }
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

    public void setOptions(Array<String> options) {
        for (String option : options) {
            titleMap.put(option, option);
            keyMap.put(option, option);
        }
        lockEvents = true;
        selectBox.setItems(options);
        lockEvents = false;
    }

    public void setOptions(String[] options) {
        for (String option : options) {
            titleMap.put(option, option);
            keyMap.put(option, option);
        }

        lockEvents = true;
        selectBox.setItems(options);
        lockEvents = false;
    }

    public Array<String> getOptions() {
        return selectBox.getItems();
    }

    @Override
    public String getValue () {
        String title = selectBox.getSelected();
        if(title == null) return "";
        String name = titleMap.get(title);

        return name;
    }

    public void setValue(String value) {
        lockEvents = true;
        selectBox.getSelection().setProgrammaticChangeEvents(false);
        selectBox.setSelected(keyMap.get(value));
        lockEvents = false;
    }

    @Override
    public void read (Json json, JsonValue jsonValue) {
        String val = jsonValue.asString();
        setValue(val);
    }

    @Override
    public void write (Json json, String name) {
        String value = "";
        if(selectBox.getSelected() != null) {
            value = titleMap.get(selectBox.getSelected());
        }
        json.writeValue(name, value);
    }
}
