package com.talosvfx.talos.editor.addons.scene.logic.metawrappers;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;
import com.talosvfx.talos.runtime.assets.AMetadata;

public class AMetaDataProvider<T extends AMetadata> implements IPropertyProvider {

	protected final T meta;

	public AMetaDataProvider (T meta) {
		this.meta = meta;
	}

	@Override
	public Array<PropertyWidget> getListOfProperties () {
		return null;
	}

	@Override
	public String getPropertyBoxTitle () {
		return null;
	}

	@Override
	public int getPriority () {
		return 1;
	}

	@Override
	public Class<? extends IPropertyProvider> getType () {
		return getClass();
	}
}
