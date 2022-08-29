package com.talosvfx.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;


public class PropertyOption {
    public final PropertyOptionType propertyOptionType;
    private final ClickListener clickListener;

    public PropertyOption(PropertyOptionType propertyOptions, ClickListener clickListener) {
        this.propertyOptionType = propertyOptions;
        this.clickListener = clickListener;
    }

    public ClickListener getClickListener() {
        return clickListener;
    }

    public String getLabel() {
        return propertyOptionType.label;
    }
}
