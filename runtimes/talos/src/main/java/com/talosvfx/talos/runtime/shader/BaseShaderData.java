package com.talosvfx.talos.runtime.shader;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.runtime.routine.RoutineInstance;
import com.talosvfx.talos.runtime.scene.utils.propertyWrappers.PropertyWrapper;

public interface BaseShaderData {
	Array<PropertyWrapper<?>> getPropertyWrappers ();

	JsonValue getJsonNodes ();

	JsonValue getJsonConnections ();

	ShaderInstance createInstance (boolean external);

	String getShaderVertexSource ();
	String getShaderFragmentSource ();

	ShaderInstance getShaderInstance ();
}
