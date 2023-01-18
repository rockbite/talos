package com.talosvfx.talos.editor.addons.scene.utils.metadata;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.runtime.assets.AMetadata;
import com.talosvfx.talos.editor.addons.scene.utils.FilePropertyProvider;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;

public class EmptyMetadata extends AMetadata {

    public EmptyMetadata() {
        super();
    }

    @Override
    public String getPropertyBoxTitle () {
        return "Prefab Settings";
    }

    @Override
    public Iterable<IPropertyProvider> getPropertyProviders () {
        Array<IPropertyProvider> propertyProviders = new Array<>();

        propertyProviders.add(new FilePropertyProvider(link.handle));

        return propertyProviders;
    }
}
