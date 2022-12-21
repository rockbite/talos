package com.talosvfx.talos.editor.layouts;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.talosvfx.talos.editor.project2.GlobalDragAndDrop;

public interface LayoutApp {

//	change tis to focus

	void setTabActive (boolean active);
	boolean isTabActive ();

	String getUniqueIdentifier ();

	void setUniqueIdentifier (String uuid);
	String getFriendlyName ();
	Actor getTabWidget ();
	Actor copyTabWidget ();

	Actor getMainContent ();
	Actor getCopyMainContent ();

	DestroyCallback getDestroyCallback ();
	void setDestroyCallback (DestroyCallback destroyCallback);

	DestroyCallback getDestroyCallback ();

	void setScrollFocus ();

	void onInputProcessorAdded ();
	void onInputProcessorRemoved ();

}
