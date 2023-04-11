package com.talosvfx.talos.editor.project2.apps;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import com.talosvfx.talos.editor.notifications.CommandEventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.Observer;
import com.talosvfx.talos.editor.notifications.commands.enums.Commands;
import com.talosvfx.talos.editor.notifications.events.commands.CommandContextEvent;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.GameObjectContainer;
import com.talosvfx.talos.editor.addons.scene.widgets.HierarchyWidget;
import com.talosvfx.talos.editor.layouts.DummyLayoutApp;
import com.talosvfx.talos.editor.project2.AppManager;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.project2.apps.preferences.ContainerOfPrefs;
import com.talosvfx.talos.editor.project2.apps.preferences.HierarchyPreference;
import com.talosvfx.talos.editor.project2.localprefs.TalosLocalPrefs;
import com.talosvfx.talos.runtime.scene.Scene;

@SingletonApp
public class SceneHierarchyApp extends AppManager.BaseApp<Scene> implements GameAsset.GameAssetUpdateListener, ContainerOfPrefs<HierarchyPreference>, Observer {

	private final HierarchyWidget hierarchyWidget;

	public SceneHierarchyApp () {
		Notifications.registerObserver(this);
		hierarchyWidget = new HierarchyWidget();
		DummyLayoutApp<Scene> hierarchyApp = new DummyLayoutApp<Scene>(SharedResources.skin, this, getAppName()) {
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

			@Override
			protected void onTouchFocused () {
				SharedResources.stage.setKeyboardFocus(hierarchyWidget.getTree());
			}
		};

		this.gridAppReference = hierarchyApp;
	}

	@Override
	public void updateForGameAsset (GameAsset<Scene> gameAsset) {
		super.updateForGameAsset(gameAsset);

		if (gameAsset.getResource() != null) {
			if (!gameAsset.listeners.contains(this, true)) {
				gameAsset.listeners.add(this);
			}

			hierarchyWidget.loadEntityContainer(gameAsset);
		}

		// apply after the three is loaded
		TalosLocalPrefs.getAppPrefs(gameAsset, this);
	}

	@Override
	public String getAppName () {
		if (gameAsset != null) {
			return "Hierarchy - " + gameAsset.nameIdentifier;
		} else {
			return "Hierarchy - ";
		}
	}

	@Override
	public void onRemove () {
			
	}

	@Override
	public void onUpdate () {
		getGridAppReference().updateTabName(getAppName());
		hierarchyWidget.loadEntityContainer(gameAsset);
	}

	@Override
	public void applyFromPreferences(HierarchyPreference prefs) {
		hierarchyWidget.getTree().collapseAll();

		// stupid hack
		// TODO: 12.01.23 fix so root is not generated everytime
		if (prefs.isRootOpen()) {
			hierarchyWidget.getTree().getRootNodes().first().setExpanded(true);
		}

		Array<String> expandedUUIDs = prefs.getUUUIDsOfExpandedObjects();
		Array<GameObject> expandedGameObjects = new Array<>();
		GameObjectContainer container = gameAsset.getResource();
		Queue<GameObject> queue = new Queue<>();

		for (GameObject gameObject : container.getGameObjects()) {
			queue.addLast(gameObject);
		}
		while (!queue.isEmpty()) {
			GameObject gameObject = queue.removeFirst();
			if (expandedUUIDs.contains(gameObject.uuid.toString(), false)) {
				expandedGameObjects.add(gameObject);
				for (GameObject child : gameObject.getGameObjects()) {
					queue.addLast(child);
				}
			}
		}
		hierarchyWidget.getTree().restoreExpandedObjects(expandedGameObjects);
	}

	@Override
	public HierarchyPreference getPrefs() {
		Array<GameObject> expandedGameObjects = new Array<>();
		hierarchyWidget.getTree().findExpandedObjects(expandedGameObjects);

		Array<String> expandedUUIDS = new Array<>();
		for (GameObject expandedGameObject : expandedGameObjects) {
			expandedUUIDS.add(expandedGameObject.uuid.toString());
		}
		HierarchyPreference preference = new HierarchyPreference();
		preference.setUUUIDsOfExpandedObjects(expandedUUIDS);

		// stupid hack
		// TODO: 12.01.23 fix so root is not generated everytime
		if (!hierarchyWidget.getTree().getRootNodes().isEmpty()) { // in case of dummy app no root node exists
			preference.setRootOpen(hierarchyWidget.getTree().getRootNodes().first().isExpanded());
		}

		return preference;
	}

	@CommandEventHandler(commandType = Commands.CommandType.DELETE)
	public void onDeleteCommand (CommandContextEvent commandContextEvent) {
		hierarchyWidget.deleteSelected();
	}

	@CommandEventHandler(commandType = Commands.CommandType.COPY)
	public void onCopyCommand (CommandContextEvent event) {
		hierarchyWidget.copySelected();
	}

	@CommandEventHandler(commandType = Commands.CommandType.PASTE)
	public void onPasteCommand (CommandContextEvent commandContextEvent) {
		hierarchyWidget.pasteFromClipboard();
	}
}

