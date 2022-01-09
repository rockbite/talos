package com.talosvfx.talos.editor.addons.scene.logic;

import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;

public interface IPropertyHolder {
    Iterable<IPropertyProvider> getPropertyProviders ();
}
