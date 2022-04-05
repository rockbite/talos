package com.talosvfx.talos;

import com.badlogic.gdx.Gdx;

import java.util.HashMap;
import java.util.Map;

public class TalosVersion {

	private static String version;

	private static Map<String, String> propValues = new HashMap<>();

	public static String getVersion () {
		if (version == null) {
			final String properties = Gdx.files.internal("talos-version.properties").readString();
			final String[] splits = properties.split("\r\n|\n");
			final int length = splits.length;
			for (int i = 0; i < length; i++) {
				final String split = splits[i];
				if (split.contains("=")) {
					final String[] keyValue = split.split("=");
					if (keyValue.length == 2) {
						propValues.put(keyValue[0], keyValue[1]);
					}
				}
			}
			if (propValues.containsKey("version")) {
				version = propValues.get("version");
			} else {
				version = "0.0.1";
			}
		}
		return version;
	}
}
