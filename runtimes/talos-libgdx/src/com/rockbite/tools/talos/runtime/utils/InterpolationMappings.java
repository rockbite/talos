package com.rockbite.tools.talos.runtime.utils;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;

public class InterpolationMappings {
	private static final ObjectMap<String, Interpolation> names = new ObjectMap<>();

	static {
		Array<String> namesArr = new Array<>();
		// get list of possible interpolations

		Field[] fields = ClassReflection.getFields(Interpolation.class);
		for (int i = 0; i < fields.length; i++) {
			try {
				Interpolation interp = (Interpolation)fields[i].get(null);
				names.put(fields[i].getName(), interp);
				namesArr.add(fields[i].getName());
			} catch (ReflectionException e) {
				e.printStackTrace();
			}
		}
	}

	public static Interpolation getInterpolationForName (String name) {
		return names.get(name);
	}

	public static String getNameForInterpolation (Interpolation interpolation) {
		for (ObjectMap.Entry<String, Interpolation> name : names) {
			if (name.value == interpolation) {
				return name.key;
			}
		}
		return "fade";
	}

	public static void getAvailableInterpolations (Array<String> interps) {
		for (String key : names.keys()) {
			interps.add(key);
		}
	}
}
