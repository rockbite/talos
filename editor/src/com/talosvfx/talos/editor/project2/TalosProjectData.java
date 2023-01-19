package com.talosvfx.talos.editor.project2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.layouts.LayoutGrid;
import com.talosvfx.talos.editor.project2.apps.ProjectExplorerApp;
import com.talosvfx.talos.editor.project2.localprefs.TalosLocalPrefs;
import com.talosvfx.talos.runtime.scene.SceneData;
import com.talosvfx.talos.editor.utils.Toasts;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TalosProjectData implements Json.Serializable {
	public static final String TALOS_PROJECT_EXTENSION = "tlsprj";
	private static final Logger logger = LoggerFactory.getLogger(TalosProjectData.class);

	//Store any specific data here for the specific project settings that should be shared with anyone that
	//is loading the same project

	@Getter@Setter
	private transient LayoutGrid layoutGrid;

	//Always non null, points to a project file that represents the root of the project
	private transient FileHandle projectFile;

	@Getter
	private SceneData sceneData = new SceneData();

	public TalosProjectData () {
		layoutGrid = new LayoutGrid(SharedResources.skin);
	}

	public static TalosProjectData loadFromFile (FileHandle projectToTryToLoad) {
		Json json = new Json();
		try {
			TalosProjectData talosProjectData = json.fromJson(TalosProjectData.class, projectToTryToLoad);
			talosProjectData.projectFile = projectToTryToLoad;
			return talosProjectData;
		} catch (Exception e) {
			logger.error("Failure to load talos project", e);
		}
		return null;
	}

	public static TalosProjectData newDefaultProject (String projectNameText, FileHandle dirHandle) {
		TalosProjectData talosProjectData = new TalosProjectData();
		//Setup the nae, make some default folders, save it

		talosProjectData.projectFile = dirHandle.child(projectNameText + "." + TALOS_PROJECT_EXTENSION);
		talosProjectData.createDefaultDirs();
		talosProjectData.createDefaultFiles();

		JsonReader jsonReader = new JsonReader();
		JsonValue jsonValue = jsonReader.parse(Gdx.files.internal("layouts/basic.json"));
		talosProjectData.jsonLayoutRepresentation = jsonValue;

		talosProjectData.save();

		return talosProjectData;
	}

	public void save () {
		Json json = new Json();
		json.setOutputType(JsonWriter.OutputType.json);
		String jsonString = json.prettyPrint(this);
		projectFile.writeString(jsonString, false);

		TalosLocalPrefs.Instance().updateProject(this);
	}

	public FileHandle rootProjectDir () {
		return projectFile.parent();
	}

	public void createDefaultFiles () {
		// create an empty scene file
		AssetRepository.getInstance().copySampleSceneToProject(rootProjectDir().child("scenes"));
	}
	private void createDefaultDirs () {
		rootProjectDir().child("scenes").mkdirs();
	}

	public String getAbsolutePathToProjectFile () {
		return projectFile.path();
	}

	@Override
	public void write (Json json) {
		json.writeValue("projectName", projectFile.nameWithoutExtension());
		json.writeValue("sceneData", sceneData);
		json.writeValue("currentLayout", layoutGrid);
	}

	@Override
	public void read (Json json, JsonValue jsonData) {
		String projectName = jsonData.getString("projectName", "NoName");
		if (jsonData.has("currentLayout")) {
			jsonLayoutRepresentation = jsonData.getChild("currentLayout");
		}
		sceneData = json.readValue("sceneData", SceneData.class, new SceneData(), jsonData);
	}

	@Getter
	private JsonValue jsonLayoutRepresentation = null;

	public void loadLayout () {
		if (jsonLayoutRepresentation != null) {
			try {
				layoutGrid.readFromJson(jsonLayoutRepresentation);
			} catch (Exception e) {
				logger.error("error loading json layout", e);
				Toasts.getInstance().showErrorToast("Error loading layout " + e.getMessage());
				SharedResources.appManager.openApp(AppManager.singletonAsset, ProjectExplorerApp.class);
			}
		} else {
			SharedResources.appManager.openApp(AppManager.singletonAsset, ProjectExplorerApp.class);
		}
	}

	public FileHandle getProjectDir() {
		String absolutePathToProjectFile = getAbsolutePathToProjectFile();
		FileHandle handle = Gdx.files.absolute(absolutePathToProjectFile).parent();

		return handle;
	}

	public boolean isPathInsideProject(String path) {
		String projectDirPath = getProjectDir().path();

		if(path.startsWith(projectDirPath)) {
			return true;
		}

		return false;
	}
}
