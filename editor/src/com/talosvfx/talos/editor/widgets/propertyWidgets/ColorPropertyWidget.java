package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.color.ColorPicker;
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;

import java.util.function.Supplier;

public class ColorPropertyWidget extends PropertyWidget<Color> {

    private Image box;
    private ColorPicker colorPicker;

    public ColorPropertyWidget(String name, Supplier<Color> supplier, ValueChanged<Color> valueChanged) {
        super(name, supplier, valueChanged);

        colorPicker = new ColorPicker("Color Picker");
        colorPicker.setListener(new ColorPickerAdapter() {
            @Override
            public void reset(Color previousColor, Color newColor) {
                super.reset(previousColor, newColor);
                box.setColor(newColor);
                callValueChanged(newColor);

            }

            @Override
            public void canceled(Color oldColor) {
                super.canceled(oldColor);
                box.setColor(oldColor);
                callValueChanged(oldColor);

            }

            @Override
            public void changed(Color newColor) {
                super.changed(newColor);
                box.setColor(newColor);
                callValueChanged(newColor);

            }
        });

        colorPicker.padTop(32);
        colorPicker.padLeft(16);
        colorPicker.setHeight(330);
        colorPicker.setWidth(430);
        colorPicker.padRight(26);
    }

    @Override
    protected void addToContainer(Actor actor) {
        valueContainer.add().expandX();
        valueContainer.add(actor).right().size(actor.getWidth(), actor.getHeight());
    }

    @Override
    public Actor getSubWidget() {
        Skin skin = SharedResources.skin;
        box = new Image(skin.newDrawable(ColorLibrary.SHAPE_SQUARE));
        box.setSize(20, 20);

        box.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                colorPicker.setColor(getValue());
                SharedResources.stage.addActor(colorPicker.fadeIn());
            }
        });

        return box;
    }

    @Override
    public void updateWidget(Color value) {
        if (value != null) {
            box.setColor(value);
        }
    }
}
