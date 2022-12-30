package com.talosvfx.talos.editor.project2.apps;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.events.commands.GONameChangeCommand;
import com.talosvfx.talos.editor.addons.scene.logic.Scene;
import com.talosvfx.talos.editor.layouts.DummyLayoutApp;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.Observer;
import com.talosvfx.talos.editor.project2.AppManager;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.project2.apps.preferences.ContainerOfPrefs;
import com.talosvfx.talos.editor.project2.apps.preferences.ViewportPreferences;
import com.talosvfx.talos.editor.project2.localprefs.TalosLocalPrefs;

import java.util.function.Consumer;
import java.util.function.Supplier;


@SingletonApp
public class SceneEditorApp extends AppManager.BaseApp<Scene> implements GameAsset.GameAssetUpdateListener, Observer, ContainerOfPrefs<ViewportPreferences> {

	private final SceneEditorWorkspace workspaceWidget;

	public SceneEditorApp () {
		this.singleton = true;

		workspaceWidget = new SceneEditorWorkspace(this);
		workspaceWidget.disableListeners();

		DummyLayoutApp sceneEditorWorkspaceApp = new DummyLayoutApp(SharedResources.skin, getAppName()) {
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
		TalosLocalPrefs.getAppPrefs(gameAsset, this);

		if (!gameAsset.listeners.contains(this, true)) {
			gameAsset.listeners.add(this);
		}


		workspaceWidget.loadFromScene(gameAsset);
		Notifications.registerObserver(this);
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

		Notifications.unregisterObserver(this);
	}

	@Override
	public void onUpdate () {
		workspaceWidget.loadFromScene(gameAsset);
	}

	@EventHandler
	public void GONameChangeCommand(GONameChangeCommand command) {
		workspaceWidget.changeGOName(command.getGo(), command.getSuggestedName());
	}

	@Override
	public Supplier<ViewportPreferences> getPreferences() {
		return workspaceWidget.getViewportViewSettings();
	}

	@Override
	public Consumer<ViewportPreferences> applyPreferences() {
		return workspaceWidget.getViewportViewSettings();
	}
}

