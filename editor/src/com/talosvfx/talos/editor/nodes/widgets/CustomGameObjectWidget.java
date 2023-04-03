package com.talosvfx.talos.editor.nodes.widgets;

import com.talosvfx.talos.editor.addons.scene.apps.routines.ui.types.ATypeWidget;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyWrapper;

public class CustomGameObjectWidget extends ATypeWidget<GameObject> {
    @Override
    public String getTypeName() {
        return "GameObject";
    }

    @Override
    public boolean isFastChange() {
        return false;
    }

    @Override
    public void updateFromPropertyWrapper(PropertyWrapper<GameObject> propertyWrapper) {

    }

    @Override
    public void applyValueToWrapper(PropertyWrapper<GameObject> propertyWrapper) {

    }
}
