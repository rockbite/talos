package com.talosvfx.talos.editor.addons.scene.logic.componentwrappers;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.runtime.scene.components.BoneComponent;

public class BoneComponentProvider extends AComponentProvider<BoneComponent> {

    public BoneComponentProvider (BoneComponent component) {
        super(component);
    }

    @Override
    public Array<PropertyWidget> getListOfProperties () {
        Array<PropertyWidget> properties = new Array<>();

        return properties;
    }

    @Override
    public String getPropertyBoxTitle () {
        return "Bone";
    }

    @Override
    public int getPriority () {
        return 10;
    }

}
