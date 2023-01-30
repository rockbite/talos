package com.talosvfx.talos.runtime.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.ObjectMap;

public class TempHackUtil {

	private static ObjectMap<String, String> replacementMaps = new ObjectMap<>();

	static {
		replacementMaps.put("com.talosvfx.talos.editor.addons.scene.logic.components", "com.talosvfx.talos.runtime.scene.components");
		replacementMaps.put("com.talosvfx.talos.runtime.modules", "com.talosvfx.talos.runtime.vfx.modules");
		replacementMaps.put("com.talosvfx.talos.editor.addons.scene.utils.propertyWrappers", "com.talosvfx.talos.runtime.scene.utils.propertyWrappers");
	}
	public static String hackIt (String asString) {
		for (ObjectMap.Entry<String, String> replacementMap : replacementMaps) {
			asString = asString.replaceAll(replacementMap.key, replacementMap.value);
		}

		return asString;
	}
}
