package com.talosvfx.talos.editor.project2.localprefs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.talosvfx.talos.editor.project2.RecentProject;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.project2.TalosProjectData;
import com.talosvfx.talos.editor.widgets.ui.menu.MainMenu;
import lombok.Data;

public class TalosLocalPrefs {

	private static final String LOCAL_PREFS = "talos_local_prefs";

	private static final Json json = new Json();

	@Data
	public static class LocalPrefData {
		private Array<RecentProject> recentProjects = new Array<>();

		public void updateProject (String absolutePathToProjectFile) {
			for (RecentProject recentProject : recentProjects) {
				if (recentProject.getProjectPath().equals(absolutePathToProjectFile)) {
					recentProject.setSaveTime(System.currentTimeMillis());
					return;
				}
			}

			RecentProject newRecentProject = new RecentProject();
			newRecentProject.setProjectPath(absolutePathToProjectFile);
			newRecentProject.setSaveTime(System.currentTimeMillis());
			recentProjects.add(newRecentProject);
		}
	}

	private LocalPrefData localPrefData;

	TalosLocalPrefs () {
		Preferences preferences = Gdx.app.getPreferences(LOCAL_PREFS);
		if (preferences.contains("localData")) {
			try {
				String localData = preferences.getString("localData");
				localPrefData = json.fromJson(LocalPrefData.class, localData);
			} catch (Exception e) {
				localPrefData = new LocalPrefData();
			}
		} else {
			localPrefData = new LocalPrefData();
		}

	}

	private void injectRecentFilesToMenu() {
		SharedResources.mainMenu.registerMenuProvider(new MainMenu.IMenuProvider() {
			@Override
			public void inject(String path, MainMenu menu) {
				Array<RecentProject> recentProjects = TalosLocalPrefs.Instance().getRecentProjects();

				for(RecentProject project: recentProjects) {
					FileHandle handle = Gdx.files.absolute(project.getProjectPath());
					menu.addItem(path, handle.path(), handle.name(), "icon-folder", handle.path());
				}
			}
		}, "file/open_recent/list");

	}

	private static TalosLocalPrefs talosLocalPrefs;

	public static TalosLocalPrefs Instance () {
		if (talosLocalPrefs == null) {
			talosLocalPrefs = new TalosLocalPrefs();

			talosLocalPrefs.injectRecentFilesToMenu();
		}
		return talosLocalPrefs;
	}

	public String getPathToLatestTalosProject () {
		if (localPrefData.recentProjects.isEmpty()) {
			return null;
		}
		Array<RecentProject> recentProjects = getRecentProjects();
		return recentProjects.first().getProjectPath();
	}


	public Array<RecentProject> getRecentProjects () {
		localPrefData.recentProjects.sort();
		return localPrefData.recentProjects;
	}

	public void updateProject (TalosProjectData talosProjectData) {
		//Let's update the recents with our latest one
		String absolutePathToProjectFile = talosProjectData.getAbsolutePathToProjectFile();

		localPrefData.updateProject(absolutePathToProjectFile);

		save();
	}

	public void save () {
		String stringData = json.toJson(localPrefData);
		Preferences prefs = Gdx.app.getPreferences(LOCAL_PREFS);
		prefs.putString("localData", stringData);
		prefs.flush();
	}

}
