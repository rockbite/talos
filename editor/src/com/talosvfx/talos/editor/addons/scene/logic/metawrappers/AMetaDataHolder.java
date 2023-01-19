package com.talosvfx.talos.editor.addons.scene.logic.metawrappers;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.logic.IPropertyHolder;
import com.talosvfx.talos.editor.addons.scene.logic.PropertyWrapperProviders;
import com.talosvfx.talos.editor.addons.scene.utils.FilePropertyProvider;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.talosvfx.talos.runtime.assets.AMetadata;

public class AMetaDataHolder<T extends AMetadata> implements IPropertyHolder {

	protected final T meta;
	private final AMetaDataProvider<T> provider;

	public AMetaDataHolder (T meta) {
		this.meta = meta;
		this.provider = (AMetaDataProvider<T>)PropertyWrapperProviders.getOrCreateProvider(meta);
	}

	@Override
	public Iterable<IPropertyProvider> getPropertyProviders () {
		Array<IPropertyProvider> propertyProviders = new Array<>();

		propertyProviders.add(new FilePropertyProvider(meta.link.handle));

		if (provider.getPropertyBoxTitle() != null) {
			propertyProviders.add(provider);
		}

		return propertyProviders;
	}

	@Override
	public String getName () {
		return meta.link.handle.name();
	}
}
