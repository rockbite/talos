package com.talosvfx.talos.editor.project2.localprefs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.editor.project2.RecentProject;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.project2.TalosProjectData;
import com.talosvfx.talos.editor.project2.apps.preferences.AppPrefs;
import com.talosvfx.talos.editor.project2.apps.preferences.ContainerOfPrefs;
import com.talosvfx.talos.editor.widgets.ui.menu.MainMenu;
import lombok.Data;
import lombok.Getter;

public class TalosLocalPrefs {

	private static final String LOCAL_PREFS = "talos_local_prefs";

	private static final String PROJECT_PREFS_PREFIX = "talos_project_prefs_";

	private static final Json json = new Json();

	@Data
	public static class LocalPrefData {
		private Array<RecentProject> recentProjects = new Array<>();

		@Getter
		private ObjectMap<String, String> globalPrefs = new ObjectMap<>();

		// TODO: 1/4/2023 overridden combinations in composition way

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

	/**
	 * Pushes data into app based on its game asset.
	 * @param gameAsset
	 * @param baseApp
	 */
	public static <T extends AppPrefs.AppPreference> void getAppPrefs (GameAsset<?> gameAsset, ContainerOfPrefs<T> baseApp) {
		String prefName = getCurrentProjectPrefName();
		Preferences preferences = Gdx.app.getPreferences(prefName);
		Class<? extends ContainerOfPrefs> clazz = baseApp.getClass();

		if (preferences.contains(clazz.getName())) {
			String prefsString = preferences.getString(clazz.getName());
			AppPrefs prefs = json.fromJson(AppPrefs.class, prefsString);
			if (prefs.hasPrefFor(gameAsset)) {
				T appPreference = (T) prefs.getPrefFor(gameAsset);
				baseApp.applyFromPreferences(appPreference);
			}
		}
	}


	/**
	 * Stores preferences based on app and its asset.
	 * Note: do not forget to call {@link #save() save} method to actually save to file.
	 * @param gameAsset
	 * @param baseApp
	 */
	public static <T extends AppPrefs.AppPreference> void setAppPrefs(GameAsset<?> gameAsset, ContainerOfPrefs<T> baseApp) {
		String prefName = getCurrentProjectPrefName();
		Preferences preferences = Gdx.app.getPreferences(prefName);
		Class<? extends ContainerOfPrefs> clazz = baseApp.getClass();

		AppPrefs appPrefs;
		T appPreference =  baseApp.getPrefs();
		// nothing to set, skip
		if (appPreference == null) {
			return;
		}
		if (!preferences.contains(clazz.getName())) {
			appPrefs = new AppPrefs();
		} else {
			String prefsString = preferences.getString(clazz.getName());
			appPrefs = json.fromJson(AppPrefs.class, prefsString);
		}
		appPrefs.setPrefFor(gameAsset, appPreference);
		String appPrefsStr = json.toJson(appPrefs);
		preferences.putString(clazz.getName(), appPrefsStr);
	}

	/**
	 * Persist the preferences.
	 */
	public static void savePrefs () {
		String prefName = getCurrentProjectPrefName();
		Preferences preferences = Gdx.app.getPreferences(prefName);
		preferences.flush();
	}

	private static String getCurrentProjectPrefName() {
		return PROJECT_PREFS_PREFIX + SharedResources.currentProject.rootProjectDir().nameWithoutExtension();
	}
}
