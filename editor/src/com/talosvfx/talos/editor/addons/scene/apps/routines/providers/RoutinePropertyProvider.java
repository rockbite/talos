package com.talosvfx.talos.editor.addons.scene.apps.routines.providers;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.data.RoutineStageData;
import com.talosvfx.talos.editor.widgets.propertyWidgets.IPropertyProvider;
import com.talosvfx.talos.editor.widgets.propertyWidgets.PropertyWidget;

public class RoutinePropertyProvider implements IPropertyProvider {

	private final RoutineStageData routineStageData;

	public RoutinePropertyProvider (RoutineStageData routineStageData) {
		this.routineStageData = routineStageData;
	}

	@Override
	public Array<PropertyWidget> getListOfProperties () {
		return null;
	}

	@Override
	public String getPropertyBoxTitle () {
		return "Routine Properties";
	}

	@Override
	public int getPriority () {
		return 0;
	}

	@Override
	public Class<? extends IPropertyProvider> getType () {
		return getClass();
	}
}
