package com.talosvfx.talos.editor.addons.scene.logic.metawrappers;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.utils.FilePropertyProvider;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.talosvfx.talos.runtime.assets.meta.PrefabMetadata;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.Prefab;

public class PrefabMetaDataHolder extends AMetaDataHolder<PrefabMetadata> {

	public PrefabMetaDataHolder (PrefabMetadata meta) {
		super(meta);
	}

	@Override
	public Iterable<IPropertyProvider> getPropertyProviders () {
		Array<IPropertyProvider> propertyProviders = new Array<>();

		propertyProviders.add(new FilePropertyProvider(meta.link.handle));


		//todo
		//		Prefab prefab = new Prefab();
//		prefab.path = link.handle.path();
//		prefab.loadFromPath();

//		GameObject root = prefab.root;
//		Iterable<IPropertyProvider> rootProviders = root.getPropertyProviders();
//		for(IPropertyProvider provider : rootProviders) {
//			propertyProviders.add(provider);
//		}

		return propertyProviders;
	}

}
