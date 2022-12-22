package com.talosvfx.talos.editor.nodes.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.XmlReader;
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;

public class ColorWidget extends AbstractWidget<Color> {

    private Color color = new Color();
    private Table colorButton;

    public void init(Skin skin, String text) {
        super.init(skin);

        Label label = null;
        if(text != null) {
            label = new Label(text, skin);
        }


        colorButton = new Table();
        colorButton.setBackground(skin.newDrawable(ColorLibrary.SHAPE_SQUIRCLE));

        if(label != null) {
            content.add(label).left().expandX().height(32);
        }
        content.add(colorButton).right().expandX().height(32).width(96);
        color.set(Color.CORAL);
        colorButton.setColor(color);

        colorButton.setTouchable(Touchable.enabled);
        colorButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                SharedResources.ui.showColorPicker(new ColorPickerAdapter() {
                    @Override
                    public void changed(Color newColor) {
                        super.changed(newColor);
                        color.set(newColor);
                        colorButton.setColor(newColor);

                        fireChangedEvent();
                    }
                });
            }
        });
    }

    @Override
    public void init(Skin skin) {
        init(skin, "Color");
    }

    @Override
    public void setColor(Color color) {
        this.color.set(color);
        colorButton.setColor(color);
    }

    @Override
    public void loadFromXML(XmlReader.Element element) {

    }

    @Override
    public Color getValue () {
        return color;
    }

    @Override
    public void read (Json json, JsonValue jsonValue) {
        color = json.readValue(Color.class, jsonValue);
        colorButton.setColor(color);
    }

    @Override
    public void write (Json json, String name) {
        json.writeValue(name, color);
    }
}
