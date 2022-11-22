package com.talosvfx.talos.editor.project2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
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

	private static TalosLocalPrefs talosLocalPrefs;

	public static TalosLocalPrefs Instance () {
		if (talosLocalPrefs == null) {
			talosLocalPrefs = new TalosLocalPrefs();
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
		//Lets update the recents with our latest one
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
