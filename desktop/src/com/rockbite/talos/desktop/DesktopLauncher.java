package com.rockbite.tools.talos.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.rockbite.tools.talos.TalosMain;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();		
		config.width = 1200;
		config.height = 700;
		new LwjglApplication(new TalosMain(), config);
	}
}
