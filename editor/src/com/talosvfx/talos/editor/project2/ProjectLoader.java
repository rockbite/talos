package com.talosvfx.talos.editor.project2;

public interface ProjectLoader {

	void loadProject (TalosProjectData projectData);

	/**
	 * Unloads currently loaded/open project.
	 */
	void unloadProject ();
}
