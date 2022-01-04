package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;

import java.util.function.Supplier;

public class ColorPropertyWidget extends PropertyWidget<Color> {

    private Image box;

    public ColorPropertyWidget() {
        super();
    }

    public ColorPropertyWidget(String name, Supplier<Color> supplier, ValueChanged<Color> valueChanged) {
        super(name, supplier, valueChanged);
    }

    @Override
    protected void addToContainer(Actor actor) {
        valueContainer.add().expandX();
        valueContainer.add(actor).right().size(actor.getWidth(), actor.getHeight());
    }

    @Override
    public Actor getSubWidget() {
        Skin skin = TalosMain.Instance().getSkin();
        box = new Image(skin.newDrawable(ColorLibrary.SHAPE_SQUARE));
        box.setSize(20, 20);
        box.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent event, float x, float y) {
                TalosMain.Instance().UIStage().showColorPicker(new ColorPickerAdapter() {
                    @Override
                    public void changed(Color newColor) {
                        super.changed(newColor);
                        box.setColor(newColor);
                        callValueChanged(box.getColor());
                    }
                });
            }
        });

        return box;
    }

    @Override
    public void updateWidget(Color value) {
        box.setColor(value);
    }
}
