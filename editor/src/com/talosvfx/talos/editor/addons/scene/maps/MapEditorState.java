package com.talosvfx.talos.editor.addons.scene.maps;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.events.GameObjectSelectionChanged;
import com.talosvfx.talos.editor.addons.scene.events.TalosLayerSelectEvent;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.MapComponent;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;

public class MapEditorState implements Notifications.Observer {

	private GameObject focusedMapObject;
	private MapComponent mapComponent;
	private TalosLayer layerSelected;

	private boolean mapFocused;

	public MapEditorState () {
		Notifications.registerObserver(this);
	}

	public void update (GameObjectSelectionChanged event) {
		Array<GameObject> gameObjects = event.get();

		if (gameObjects.size == 0) {
			unfocusMap();
		} else if (gameObjects.size == 1) {
			GameObject first = gameObjects.first();
			if (first.hasComponentType(MapComponent.class)) {
				focusMap(first);
			} else {
				unfocusMap();
			}
		}
	}

	@EventHandler
	public void onLayerSelect (TalosLayerSelectEvent event) {
		selectLayer(event.layer);
	}

	private void selectLayer (TalosLayer layer) {
		boolean shouldShow = false;
		if (this.layerSelected == null) {
			shouldShow = true;
		}
		this.layerSelected = layer;
		if (shouldShow) {
			SceneEditorWorkspace.getInstance().showMapEditToolbar();
		}
	}

	private void focusMap (GameObject mapObject) {
		focusedMapObject = mapObject;
		mapComponent = mapObject.getComponent(MapComponent.class);
		mapFocused = true;
	}

	private void unfocusMap () {
		if (layerSelected != null) {
			//Hide the edit window tab
			SceneEditorWorkspace.getInstance().hideMapEditToolbar();
		}
		if (mapFocused) {
			mapFocused = false;
			layerSelected = null;
			mapComponent = null;
			focusedMapObject = null;
		}
	}

	public boolean isEditing () {
		return layerSelected != null;
	}
}
