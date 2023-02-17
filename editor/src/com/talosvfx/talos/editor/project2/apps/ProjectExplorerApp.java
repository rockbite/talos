package com.talosvfx.talos.editor.project2.apps;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.talosvfx.talos.editor.project2.apps.preferences.ContainerOfPrefs;
import com.talosvfx.talos.editor.project2.apps.preferences.ProjectExplorerPreferences;
import com.talosvfx.talos.editor.project2.localprefs.TalosLocalPrefs;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.widgets.ProjectExplorerWidget;
import com.talosvfx.talos.editor.layouts.DummyLayoutApp;
import com.talosvfx.talos.editor.notifications.CommandEventHandler;
import com.talosvfx.talos.editor.notifications.commands.enums.Commands;
import com.talosvfx.talos.editor.notifications.events.commands.CommandContextEvent;
import com.talosvfx.talos.editor.project2.AppManager;
import com.talosvfx.talos.editor.project2.SharedResources;

@SingletonApp
public class ProjectExplorerApp extends AppManager.BaseApp<Object> implements ContainerOfPrefs<ProjectExplorerPreferences> {

	private final ProjectExplorerWidget projectExplorerWidget;

	public ProjectExplorerApp () {
		this.singleton = true;

		super.updateForGameAsset(gameAsset);

		projectExplorerWidget = new ProjectExplorerWidget();
		DummyLayoutApp<Object> assetDirectoryApp = new DummyLayoutApp<Object>(SharedResources.skin, this, getAppName()) {
			@Override
			public Actor getMainContent () {
				return projectExplorerWidget;
			}

			@Override
			public void onInputProcessorAdded () {
				super.onInputProcessorAdded();
				SharedResources.stage.setScrollFocus(projectExplorerWidget.getDirectoryViewWidget().getScrollPane());
			}

			@Override
			public void onInputProcessorRemoved () {
				super.onInputProcessorRemoved();
			}
		};

		this.gridAppReference = assetDirectoryApp;
	}

	@Override
	public void updateForGameAsset (GameAsset<Object> gameAsset) {
		super.updateForGameAsset(gameAsset);
		TalosLocalPrefs.getAppPrefs(gameAsset, this);

		projectExplorerWidget.loadDirectoryTree(SharedResources.currentProject.rootProjectDir().path());

	}

	@Override
	public String getAppName () {
		return "Explorer";
	}

	@Override
	public void onRemove () {

	}

	@CommandEventHandler(commandType = Commands.CommandType.COPY)
	public void onCopyCommand (CommandContextEvent commandContextEvent) {
		projectExplorerWidget.getDirectoryViewWidget().invokeCopy();
	}

	@CommandEventHandler(commandType = Commands.CommandType.PASTE)
	public void onPasteCommand (CommandContextEvent commandContextEvent) {
		projectExplorerWidget.getDirectoryViewWidget().invokePaste();
	}

	@CommandEventHandler(commandType = Commands.CommandType.CUT)
	public void onCutCommand (CommandContextEvent commandContextEvent) {
		projectExplorerWidget.getDirectoryViewWidget().invokeCut();
	}

	@CommandEventHandler(commandType = Commands.CommandType.SELECT_ALL)
	public void onSelectAllCommand (CommandContextEvent commandContextEvent) {
		projectExplorerWidget.getDirectoryViewWidget().invokeSelectAll();
	}

	@CommandEventHandler(commandType = Commands.CommandType.DELETE)
	public void onDeleteCommand (CommandContextEvent commandContextEvent) {
		projectExplorerWidget.getDirectoryViewWidget().invokeDelete();
	}

	@CommandEventHandler(commandType = Commands.CommandType.RENAME)
	public void onRenameCommand (CommandContextEvent commandContextEvent) {
		projectExplorerWidget.getDirectoryViewWidget().invokeDelete();
	}


	public FileHandle getCurrentSelectedFolder() {
		return projectExplorerWidget.getCurrentFolder();
	}

	@Override
	public void applyFromPreferences(ProjectExplorerPreferences prefs) {
		projectExplorerWidget.getSplitPane().setSplitAmount(prefs.sidebarSplitAmount);
	}

	@Override
	public ProjectExplorerPreferences getPrefs() {
		ProjectExplorerPreferences preferences = new ProjectExplorerPreferences();
		preferences.sidebarSplitAmount = projectExplorerWidget.getSplitPane().getSplitAmount();
		return preferences;
	}
}

