package com.talosvfx.talos.editor.addons.scene.apps.routines.ui.types;

import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.talosvfx.talos.editor.addons.scene.utils.propertyWrappers.PropertyWrapper;
import com.talosvfx.talos.editor.project2.SharedResources;

public class CustomBooleanWidget extends ATypeWidget<Boolean>  {

    private final CheckBox checkBox;

    public CustomBooleanWidget() {
        checkBox = new CheckBox("", SharedResources.skin, "panel-checkbox");

        Table table = new Table();

        table.add(new Label("value", SharedResources.skin)).left().expandX();
        table.add(checkBox).right().expandX();

        add(table).padLeft(4).padRight(4).width(220).padTop(9).padBottom(5);
    }

    @Override
    public String getTypeName() {
        return "bool";
    }

    @Override
    public void updateFromPropertyWrapper(PropertyWrapper<Boolean> propertyWrapper) {
        checkBox.setChecked(propertyWrapper.defaultValue);
    }

    @Override
    public void applyValueToWrapper(PropertyWrapper<Boolean> propertyWrapper) {
        propertyWrapper.defaultValue = checkBox.isChecked();
    }
}
