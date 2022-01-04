package com.talosvfx.talos;

import com.badlogic.gdx.Gdx;

import java.io.IOException;
import java.util.Properties;

public class TalosVersion {

	private static String version;

	public static String getVersion () {
		if (version == null) {
			Properties properties = new Properties();
			try {
				properties.load(Gdx.files.internal("talos-version.properties").read());
				version = properties.getProperty("version");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return version;
	}
}
