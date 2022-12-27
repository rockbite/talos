package com.talosvfx.talos.editor.project2.localprefs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.editor.project2.RecentProject;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.project2.TalosProjectData;
import com.talosvfx.talos.editor.widgets.ui.menu.MainMenu;
import lombok.Data;
import lombok.Getter;

public class TalosLocalPrefs {

	private static final String LOCAL_PREFS = "talos_local_prefs";

	private static final Json json = new Json();

	@Data
	public static class LocalPrefData {
		private Array<RecentProject> recentProjects = new Array<>();

		@Getter
		private ObjectMap<String, String> globalPrefs = new ObjectMap<>();

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

		// reevaluate this list
		boolean listUpdated = false;
		for(int i = localPrefData.recentProjects.size - 1; i >= 0; i--) {
			RecentProject recentProject = localPrefData.recentProjects.get(i);
			if(!Gdx.files.absolute(recentProject.getProjectPath()).exists()) {
				localPrefData.recentProjects.removeIndex(i);
				listUpdated = true;
			}
		}

		if(listUpdated) {
			save();
		}

		localPrefData.recentProjects.sort();
		return localPrefData.recentProjects;
	}

	public void setGlobalData(String key, String value) {
		localPrefData.globalPrefs.put(key, value);
	}

	public String getGlobalData(String key) {
		return localPrefData.globalPrefs.get(key, "");
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
