package com.talosvfx.talos.editor.addons.scene.logic.componentwrappers;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.logic.PropertyWrapperProviders;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.talosvfx.talos.runtime.scene.Scene;
import lombok.Getter;

public class ScenePropertyHolder extends PropertyWrapperProviders.ObjectPropertyHolder<Scene> {

	@Getter
	private final Scene scene;
	private final ScenePropertyProvider scenePropertyProvider;

	public ScenePropertyHolder (Scene scene) {
		this.scene = scene;
		scenePropertyProvider = new ScenePropertyProvider(scene);
	}

	@Override
	public Iterable<IPropertyProvider> getPropertyProviders () {
		Array<IPropertyProvider> list = new Array<>();

		list.add(scenePropertyProvider);

		return list;
	}

	@Override
	public String getName () {
		return scene.getName();
	}
}
