package com.talosvfx.talos.editor.addons.scene.logic.components;

import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;

public abstract class AComponent implements IPropertyProvider {

    @Override
    public Class<? extends IPropertyProvider> getType () {
        return getClass();
    }

}
