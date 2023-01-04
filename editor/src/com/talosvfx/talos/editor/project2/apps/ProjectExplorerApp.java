package com.talosvfx.talos.editor.project2.apps;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.logic.Scene;
import com.talosvfx.talos.editor.addons.scene.widgets.ProjectExplorerWidget;
import com.talosvfx.talos.editor.addons.scene.widgets.PropertyPanel;
import com.talosvfx.talos.editor.layouts.DummyLayoutApp;
import com.talosvfx.talos.editor.project2.AppManager;
import com.talosvfx.talos.editor.project2.SharedResources;

@SingletonApp
public class ProjectExplorerApp extends AppManager.BaseApp<Object> {

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
		projectExplorerWidget.loadDirectoryTree(SharedResources.currentProject.rootProjectDir().path());

	}

	@Override
	public String getAppName () {
		return "Explorer";
	}

	@Override
	public void onRemove () {

	}

	public FileHandle getCurrentSelectedFolder() {
		return projectExplorerWidget.getCurrentFolder();
	}
}

