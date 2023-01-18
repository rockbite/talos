package com.talosvfx.talos.editor.addons.scene.utils.metadata;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.runtime.scene.GameObject;import com.talosvfx.talos.editor.addons.scene.logic.Prefab;
import com.talosvfx.talos.runtime.assets.AMetadata;
import com.talosvfx.talos.editor.addons.scene.utils.FilePropertyProvider;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;

public class PrefabMetadata extends AMetadata {

    public PrefabMetadata() {
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

        Prefab prefab = new Prefab(link.handle);

        GameObject root = prefab.root;
        Iterable<IPropertyProvider> rootProviders = root.getPropertyProviders();
        for(IPropertyProvider provider : rootProviders) {
            propertyProviders.add(provider);
        }

        return propertyProviders;
    }
}
