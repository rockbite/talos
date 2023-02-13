package com.talosvfx.talos.editor.nodes.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.XmlReader;
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;
import com.talosvfx.talos.editor.widgets.ui.common.zoomWidgets.LabelWithZoom;

public class ColorWidget extends AbstractWidget<Color> {

    private Color color = new Color();
    private Table colorButton;

    private boolean isBeingEdited = false;

    public void init(Skin skin, String text) {
        super.init(skin);

        Label label = null;
        if(text != null) {
            label = new LabelWithZoom(text, skin);
        }


        colorButton = new Table();
        colorButton.setBackground(skin.newDrawable(ColorLibrary.SHAPE_SQUIRCLE));

        if(label != null) {
            content.add(label).left().expandX().height(32);
        }

        Table buttonWrapper = new Table();
        buttonWrapper.setBackground(skin.newDrawable("color-bg"));
        buttonWrapper.add(colorButton).center().pad(2f).grow();

        content.add(buttonWrapper).right().expandX().height(32).width(96);
        color.set(Color.WHITE);
        colorButton.setColor(color);

        colorButton.setTouchable(Touchable.enabled);
        colorButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                SharedResources.ui.showColorPicker(new ColorPickerAdapter() {
                    @Override
                    public void changed(Color newColor) {
                        super.changed(newColor);
                        isBeingEdited = true;
                        color.set(newColor);
                        colorButton.setColor(newColor);

                        fireChangedEvent();
                    }

                    @Override
                    public void canceled(Color oldColor) {
                        super.canceled(oldColor);
                        isBeingEdited = false;
                        fireChangedEvent();
                    }

                    @Override
                    public void reset(Color previousColor, Color newColor) {
                        super.reset(previousColor, newColor);
                        isBeingEdited = false;
                        fireChangedEvent();
                    }

                    @Override
                    public void finished(Color newColor) {
                        super.finished(newColor);
                        isBeingEdited = false;
                        fireChangedEvent();
                    }
                });
            }
        });
    }

    @Override
    public boolean isFastChange() {
        return isBeingEdited;
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
