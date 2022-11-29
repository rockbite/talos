package com.talosvfx.talos.editor.project2.apps;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.logic.Scene;
import com.talosvfx.talos.editor.addons.scene.widgets.HierarchyWidget;
import com.talosvfx.talos.editor.layouts.DummyLayoutApp;
import com.talosvfx.talos.editor.project2.AppManager;
import com.talosvfx.talos.editor.project2.SharedResources;

@SingletonApp
public class SceneHierarchyApp extends AppManager.BaseApp<Scene> {

	private final HierarchyWidget hierarchyWidget;

	public SceneHierarchyApp () {
		this.singleton = true;


		hierarchyWidget = new HierarchyWidget();
		DummyLayoutApp hierarchyApp = new DummyLayoutApp(SharedResources.skin, getAppName()) {
			@Override
			public Actor getMainContent () {
				return hierarchyWidget;
			}

			@Override
			public void onInputProcessorAdded () {
				super.onInputProcessorAdded();
				SharedResources.stage.setScrollFocus(hierarchyWidget.getScrollPane());
			}

			@Override
			public void onInputProcessorRemoved () {
				super.onInputProcessorRemoved();
			}
		};

		this.gridAppReference = hierarchyApp;
	}

	@Override
	public void updateForGameAsset (GameAsset<Scene> gameAsset) {
		super.updateForGameAsset(gameAsset);

		hierarchyWidget.loadEntityContainer(gameAsset.getResource());
	}

	@Override
	public String getAppName () {
		if (gameAsset != null) {
			return "Hierarchy - " + gameAsset.nameIdentifier;
		} else {
			return "Hierarchy - ";
		}
	}
}

