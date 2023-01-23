package com.talosvfx.talos.editor.addons.scene.logic.componentwrappers;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.logic.PropertyWrapperProviders;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.talosvfx.talos.runtime.scene.Prefab;

public class PrefabPropertyHolder extends PropertyWrapperProviders.ObjectPropertyHolder<Prefab> {

	private final Prefab prefab;

	public PrefabPropertyHolder(Prefab prefab) {
		this.prefab = prefab;
	}

	@Override
	public Iterable<IPropertyProvider> getPropertyProviders () {
		Array<IPropertyProvider> list = new Array<>();

		return list;
	}

	@Override
	public String getName () {
		return prefab.getName();
	}
}
