package com.talosvfx.talos.editor.project2.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.events.save.SaveRequest;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.project2.TalosProjectData;
import com.talosvfx.talos.editor.project2.localprefs.TalosLocalPrefs;
import com.talosvfx.talos.editor.utils.InputUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class Shortcuts extends InputAdapter {

	@Override
	public boolean keyDown (int keycode) {
		//todo make system

		if (keycode == Input.Keys.S && (InputUtils.ctrlPressed() || InputUtils.macCmdPressed())) {
			SaveRequest saveRequest = Notifications.obtainEvent(SaveRequest.class);
			Notifications.fireEvent(saveRequest);
			return true;
		}

		if (keycode == Input.Keys.Z && (InputUtils.ctrlPressed() || InputUtils.macCmdPressed())) {
			SharedResources.globalSaveStateSystem.onUndoRequest();
		}

		if (keycode == Input.Keys.E && (InputUtils.ctrlPressed() || InputUtils.macCmdPressed())) {
			//todo: delegate this to export action
			AssetRepository.getInstance().exportToFile();

			TalosProjectData currentProject = SharedResources.currentProject;
			Preferences projectPrefs = TalosLocalPrefs.Instance().getProjectPrefs();
			String exportScript = projectPrefs.getString("project.general.exportScript", "");
			String projectFilePath = projectPrefs.getString("project.general.exportPath", "");
			if(!exportScript.isEmpty()) {
				String projectPath = currentProject.rootProjectDir().path();

				FileHandle handle = Gdx.files.absolute(projectPath + File.separator + exportScript);

				if (handle.exists() && !handle.isDirectory()) {
					Runtime rt = Runtime.getRuntime();

					try {
						String nodeCommand = "node";
						String buildScriptPath = handle.path();
						String projectDirectoryPath = "\"" + projectPath  + "\"";
						String projectFilePathComm = "\"" + projectFilePath + "\"";

						if (TalosMain.Instance().isOsX()) {
							File nodeBinary = new File(nodeCommand);
							if(!nodeBinary.exists()) {
								nodeCommand = "/opt/homebrew/bin/node";
							}
							ProcessBuilder pb = new ProcessBuilder("bash", "-l", "-c", nodeCommand + " " + buildScriptPath + " " + projectDirectoryPath + " " + projectFilePathComm);
							pb.inheritIO();
							pb.start();
						} else {
							ProcessBuilder pb = new ProcessBuilder(nodeCommand, buildScriptPath, projectDirectoryPath, projectFilePathComm);
							pb.inheritIO();
							pb.start();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		return false;

	}
}
