package com.talosvfx.talos.editor.layouts;

import com.badlogic.gdx.scenes.scene2d.Actor;

public interface LayoutApp {

//	change tis to focus

	void setTabActive (boolean active);

	String getUniqueIdentifier ();
	Actor getTabWidget ();
	Actor copyTabWidget ();

	Actor getMainContent ();
	Actor getCopyMainContent ();

	void setDestroyCallback (DestroyCallback destroyCallback);

}
