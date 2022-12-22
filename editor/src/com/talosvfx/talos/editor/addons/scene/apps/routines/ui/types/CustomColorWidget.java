package com.talosvfx.talos.editor.addons.scene.apps.routines.ui.types;

import com.talosvfx.talos.editor.addons.scene.utils.propertyWrappers.PropertyWrapper;
import com.talosvfx.talos.editor.nodes.widgets.ColorWidget;
import com.talosvfx.talos.editor.project2.SharedResources;

public class CustomColorWidget extends ATypeWidget {

    private final ColorWidget colorWidget;

    @Override
    public String getTypeName() {
        return "color";
    }

    @Override
    public void applyValueToWrapper(PropertyWrapper<?> propertyWrapper) {

    }

    public CustomColorWidget() {
        colorWidget = new ColorWidget();
        colorWidget.init(SharedResources.skin, "color");

        add(colorWidget).padLeft(4).padRight(4).width(220).padTop(9).padBottom(5);
    }
}
