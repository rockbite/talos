package com.talosvfx.talos.editor.layouts;

import com.badlogic.gdx.scenes.scene2d.Actor;

public interface LayoutApp {

	String getUniqueIdentifier ();
	Actor getTabWidget ();
	Actor copyTabWidget ();

	Actor getMainContent ();

}
