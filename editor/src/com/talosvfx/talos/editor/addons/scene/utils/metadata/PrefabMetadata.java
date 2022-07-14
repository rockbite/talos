package com.talosvfx.talos.editor.addons.scene.utils.metadata;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.Prefab;
import com.talosvfx.talos.editor.addons.scene.utils.AMetadata;
import com.talosvfx.talos.editor.addons.scene.utils.FilePropertyProvider;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;

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

        propertyProviders.add(new FilePropertyProvider(currentFile));

        Prefab prefab = new Prefab();
        prefab.path = currentFile.path();
        prefab.loadFromPath();

        GameObject root = prefab.root;
        Iterable<IPropertyProvider> rootProviders = root.getPropertyProviders();
        for(IPropertyProvider provider : rootProviders) {
            propertyProviders.add(provider);
        }

        return propertyProviders;
    }
}
