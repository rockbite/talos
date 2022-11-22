package com.talosvfx.talos.editor.project2;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.widgets.HierarchyWidget;
import com.talosvfx.talos.editor.addons.scene.widgets.ProjectExplorerWidget;
import com.talosvfx.talos.editor.addons.scene.widgets.PropertyPanel;
import com.talosvfx.talos.editor.addons.scene.widgets.directoryview.DirectoryViewWidget;
import com.talosvfx.talos.editor.layouts.DummyLayoutApp;
import com.talosvfx.talos.editor.layouts.LayoutContent;
import com.talosvfx.talos.editor.layouts.LayoutGrid;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class TalosProjectData implements Json.Serializable {
	public static final String TALOS_PROJECT_EXTENSION = "tlsprj";
	private static final Logger logger = LoggerFactory.getLogger(TalosProjectData.class);

	//Store any specific data here for the specific project settings that should be shared with anyone that
	//is loading the same project

	@Getter
	private transient LayoutGrid layoutGrid;


	//Always non null, points to a project file that represents the root of the project
	private transient FileHandle projectFile;



	public TalosProjectData () {
		layoutGrid = new LayoutGrid(SharedResources.skin);

		test();
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

		talosProjectData.save();

		return talosProjectData;
	}

	private void save () {
		Json json = new Json();
		String jsonString = json.prettyPrint(this);
		projectFile.writeString(jsonString, false);

		TalosLocalPrefs.Instance().updateProject(this);
	}

	public FileHandle rootProjectDir () {
		return projectFile.parent();
	}
	private void createDefaultDirs () {
		rootProjectDir().child("scenes").mkdirs();
		rootProjectDir().child("textures").mkdirs();
		rootProjectDir().child("models").mkdirs();
		rootProjectDir().child("vfx").mkdirs();
	}

	public void test () {
		layoutGrid.reset();


		{
			LayoutContent sceneEditorWorkspace = new LayoutContent(SharedResources.skin, layoutGrid);

			SceneEditorWorkspace workspaceWidget = new SceneEditorWorkspace();
			//Find a scene and open it

			sceneEditorWorkspace.addContent(new DummyLayoutApp(SharedResources.skin, "Scene") {
				@Override
				public Actor getMainContent () {
					return workspaceWidget;
				}
			});

			layoutGrid.addContent(sceneEditorWorkspace);
		}
		{
			LayoutContent directoryViewContent = new LayoutContent(SharedResources.skin, layoutGrid);
			ProjectExplorerWidget projectExplorerWidget = new ProjectExplorerWidget();
			directoryViewContent.addContent(new DummyLayoutApp(SharedResources.skin, "Assets") {
				@Override
				public Actor getMainContent () {
					return projectExplorerWidget;
				}
			});

			layoutGrid.addContent(directoryViewContent);

		}
		{
			LayoutContent propertiesWidget = new LayoutContent(SharedResources.skin, layoutGrid);

			PropertyPanel propertyPanel = new PropertyPanel();
			propertiesWidget.addContent(new DummyLayoutApp(SharedResources.skin, "Properties") {
				@Override
				public Actor getMainContent () {
					return propertyPanel;
				}
			});

			layoutGrid.addContent(propertiesWidget);

		}
		{
			LayoutContent hierarchyView = new LayoutContent(SharedResources.skin, layoutGrid);

			HierarchyWidget hierarchyWidget = new HierarchyWidget();
			hierarchyView.addContent(new DummyLayoutApp(SharedResources.skin, "Hierarchy") {
				@Override
				public Actor getMainContent () {
					return hierarchyWidget;
				}
			});

			layoutGrid.addContent(hierarchyView);

		}
	}


	public String getAbsolutePathToProjectFile () {
		return projectFile.path();
	}

	@Override
	public void write (Json json) {
		json.writeValue("projectName", projectFile.nameWithoutExtension());
	}

	@Override
	public void read (Json json, JsonValue jsonData) {
		String projectName = jsonData.getString("projectName", "NoName");

	}
}
