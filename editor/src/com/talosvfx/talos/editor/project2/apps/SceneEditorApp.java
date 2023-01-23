package com.talosvfx.talos.editor.project2.apps;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.editor.layouts.DummyLayoutApp;
import com.talosvfx.talos.editor.project2.AppManager;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.project2.apps.preferences.ContainerOfPrefs;
import com.talosvfx.talos.editor.project2.apps.preferences.ViewportPreferences;
import com.talosvfx.talos.editor.project2.localprefs.TalosLocalPrefs;
import com.talosvfx.talos.runtime.scene.Scene;
import lombok.Getter;


@SingletonApp
public class SceneEditorApp extends AppManager.BaseApp<Scene> implements GameAsset.GameAssetUpdateListener, ContainerOfPrefs<ViewportPreferences> {

	@Getter
	private final SceneEditorWorkspace workspaceWidget;

	public SceneEditorApp () {
		this.singleton = true;

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
	}

	@Override
	public void onUpdate () {
		workspaceWidget.loadFromScene(gameAsset);
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

