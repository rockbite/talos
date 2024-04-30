package com.talosvfx.talos.runtime.routine.serialization;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.routine.RoutineInstance;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyWrapper;

public interface BaseRoutineData {
	Array<PropertyWrapper<?>> getPropertyWrappers ();

	JsonValue getJsonNodes ();

	JsonValue getJsonConnections ();

	RoutineInstance createInstance (boolean external, String talosIdentifier);
}
