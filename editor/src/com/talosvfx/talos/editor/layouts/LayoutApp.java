package com.talosvfx.talos.editor.layouts;

import com.badlogic.gdx.scenes.scene2d.Actor;

public interface LayoutApp {

//	change tis to focus

	void setTabActive (boolean active);

	boolean isTabActive ();

	void setTabFocused (boolean focused);

	boolean isTabFocused ();

	String getUniqueIdentifier ();

	void setUniqueIdentifier (String uuid);
	String getFriendlyName ();
	Actor getTabWidget ();
	Actor copyTabWidget ();

	Actor getMainContent ();
	Actor getCopyMainContent ();

	void setDestroyCallback (DestroyCallback destroyCallback);

	DestroyCallback getDestroyCallback ();

	void setScrollFocus ();

	void onInputProcessorAdded ();
	void onInputProcessorRemoved ();

	void updateTabName(String name);

	void setLayoutContent(LayoutContent layoutContent);
	LayoutContent getLayoutContent();
	void actInBackground(float delta);

	boolean hasPreferredWidth();

	boolean hasPreferredHeight();
}
