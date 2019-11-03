package com.rockbite.tools.talos.editor.widgets.propertyWidgets;

import com.badlogic.gdx.utils.Array;

public abstract class StringListProperty extends MutableProperty<String> {

    public StringListProperty(String propertyName, String initialValue) {
        super(propertyName, initialValue);
    }

    public abstract Array<String> getOptionsList();

    @Override
    public SelectBoxWidget getPropertyWidgetClass() {
        return new SelectBoxWidget();
    }
}
