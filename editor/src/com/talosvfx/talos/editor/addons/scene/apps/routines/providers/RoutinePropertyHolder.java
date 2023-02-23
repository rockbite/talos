package com.talosvfx.talos.editor.addons.scene.apps.routines.providers;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.logic.PropertyWrapperProviders;
import com.talosvfx.talos.editor.data.RoutineStageData;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;

public class RoutinePropertyHolder extends PropertyWrapperProviders.ObjectPropertyHolder<RoutineStageData> {

	private final RoutineStageData routineStageData;
	private final RoutinePropertyProvider routinePropertyProvider;

	public RoutinePropertyHolder (RoutineStageData routineStageData) {
		this.routineStageData = routineStageData;
		routinePropertyProvider = new RoutinePropertyProvider(routineStageData);
	}

	@Override
	public Iterable<IPropertyProvider> getPropertyProviders () {
		Array<IPropertyProvider> list = new Array<>();

		list.add(routinePropertyProvider);

		return list;
	}

	@Override
	public String getName () {
		return routineStageData.getName();
	}
}
