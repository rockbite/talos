package com.talosvfx.talos.editor.addons.scene.apps.routines.ui.types;

import com.badlogic.gdx.graphics.Color;
import com.talosvfx.talos.editor.nodes.widgets.ColorWidget;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyWrapper;

public class CustomColorWidget extends ATypeWidget<Color> {

    private final ColorWidget colorWidget;

    @Override
    public String getTypeName() {
        return "color";
    }

    @Override
    public void updateFromPropertyWrapper(PropertyWrapper<Color> propertyWrapper) {
        colorWidget.setColor(propertyWrapper.defaultValue);
    }

    @Override
    public void applyValueToWrapper(PropertyWrapper<Color> propertyWrapper) {
        propertyWrapper.defaultValue.set(colorWidget.getValue());
    }

    public CustomColorWidget() {
        colorWidget = new ColorWidget();
        colorWidget.init(SharedResources.skin, "color");

        add(colorWidget).padLeft(4).padRight(4).width(220).padTop(9).padBottom(5);
    }
}
