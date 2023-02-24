package com.talosvfx.talos.editor.project2.apps;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.events.commands.GONameChangeCommand;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.Observer;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.editor.layouts.DummyLayoutApp;
import com.talosvfx.talos.editor.notifications.CommandEventHandler;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.commands.enums.Commands;
import com.talosvfx.talos.editor.notifications.events.commands.CommandContextEvent;
import com.talosvfx.talos.editor.project2.AppManager;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.project2.apps.preferences.ContainerOfPrefs;
import com.talosvfx.talos.editor.project2.apps.preferences.ViewportPreferences;
import com.talosvfx.talos.editor.project2.localprefs.TalosLocalPrefs;
import com.talosvfx.talos.runtime.scene.Scene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.Getter;


@SingletonApp
public class SceneEditorApp extends AppManager.BaseApp<Scene> implements GameAsset.GameAssetUpdateListener, ContainerOfPrefs<ViewportPreferences>, Observer {

	@Getter
	private final SceneEditorWorkspace workspaceWidget;

	private static final Logger logger = LoggerFactory.getLogger(SceneEditorApp.class);

	public SceneEditorApp () {
		this.singleton = true;

		Notifications.registerObserver(this);
		workspaceWidget = new SceneEditorWorkspace(this);
		workspaceWidget.disableListeners();

		DummyLayoutApp<Scene> sceneEditorWorkspaceApp = new DummyLayoutApp<Scene>(SharedResources.skin, this, getAppName()) {
			@Override
			public Actor getMainContent () {
				return workspaceWidget;
			}

			@Override
			public void onInputProcessorAdded () {
				super.onInputProcessorAdded();
				workspaceWidget.restoreListeners();
				SharedResources.stage.setScrollFocus(workspaceWidget);
			}

			@Override
			public void onInputProcessorRemoved () {
				super.onInputProcessorRemoved();
				workspaceWidget.disableListeners();
			}

			@Override
			protected void onTouchFocused () {
				SharedResources.stage.setKeyboardFocus(workspaceWidget);
			}
		};

		this.gridAppReference = sceneEditorWorkspaceApp;
	}

	@Override
	public void updateForGameAsset (GameAsset<Scene> gameAsset) {
		super.updateForGameAsset(gameAsset);

		// TODO: 23.02.23 dummy refactor
		if (AppManager.dummyAsset == (GameAsset) gameAsset) {
			return;
		}

		TalosLocalPrefs.getAppPrefs(gameAsset, this);

		if (!gameAsset.listeners.contains(this, true)) {
			gameAsset.listeners.add(this);
		}


		workspaceWidget.loadFromScene(gameAsset);
	}

	@Override
	public String getAppName () {
		if (gameAsset != null) {
			return "Scene - " + gameAsset.nameIdentifier;
		} else {
			return "Scene - ";
		}
	}

	@Override
	public void onRemove () {
		gameAsset.listeners.removeValue(this, true);
		Notifications.registerObserver(this);
	}

	@Override
	public void onUpdate () {
		getGridAppReference().updateTabName(getAppName());
		workspaceWidget.loadFromScene(gameAsset);
	}

	@CommandEventHandler(commandType = Commands.CommandType.COPY)
	public void onCopyCommand (CommandContextEvent event) {
		workspaceWidget.copySelected();
	}

	@CommandEventHandler(commandType = Commands.CommandType.PASTE)
	public void onPasteCommand (CommandContextEvent commandContextEvent) {
		workspaceWidget.pasteFromClipboard();
	}

	@CommandEventHandler(commandType = Commands.CommandType.SELECT_ALL)
	public void onSelectAllCommand (CommandContextEvent commandContextEvent) {
		workspaceWidget.selectAll();
	}

	@CommandEventHandler(commandType = Commands.CommandType.GROUP)
	public void onGroupCommand (CommandContextEvent commandContextEvent) {
		workspaceWidget.convertSelectedIntoGroup();
	}

	@CommandEventHandler(commandType = Commands.CommandType.DELETE)
	public void onDeleteCommand (CommandContextEvent commandContextEvent) {
		workspaceWidget.deleteSelected();
	}

	@CommandEventHandler(commandType = Commands.CommandType.ESCAPE)
	public void onEscapeCommand (CommandContextEvent commandContextEvent) {
		workspaceWidget.escapePressed();
	}

	@Override
	public void applyFromPreferences(ViewportPreferences prefs) {
		workspaceWidget.setCameraPos(prefs.cameraPos);
		workspaceWidget.setCameraZoom(prefs.cameraZoom);
	}

	@Override
	public ViewportPreferences getPrefs() {
		ViewportPreferences prefs = new ViewportPreferences();
		prefs.cameraPos = workspaceWidget.getCameraPos();
		prefs.cameraZoom = workspaceWidget.getCameraZoom();
		return prefs;
	}
}

