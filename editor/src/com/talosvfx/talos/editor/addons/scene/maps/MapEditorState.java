package com.talosvfx.talos.editor.addons.scene.maps;

import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.apps.tiledpalette.PaletteEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.events.GameObjectSelectionChanged;
import com.talosvfx.talos.editor.addons.scene.events.TalosLayerSelectEvent;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.MapComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TileDataComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;

public class MapEditorState implements Notifications.Observer {

	private GameObject focusedMapObject;
	private MapComponent mapComponent;
	private TalosLayer layerSelected;

	private GameObject gameObjectWeArePainting;

	private boolean mapFocused;

	private boolean painting = false;
	private boolean erasing = false;

	public MapEditorState () {
		Notifications.registerObserver(this);
	}

	public boolean isPainting () {
		return painting;
	}

	public boolean isErasing () {
		return erasing;
	}

	public void setPainting (boolean painting) {
		this.painting = painting;
		if (painting) {
			showDrawingObject();
		} else {
			hideDrawingObject();
		}
	}

	public void setErasing (boolean erasing) {
		this.erasing = erasing;
		hideDrawingObject();
	}

	public void update (GameObjectSelectionChanged event) {
		Array<GameObject> gameObjects = event.get();

		if (event.getContext() instanceof SceneEditorWorkspace) {
			//Changed selection in main window

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

		} else if (event.getContext() instanceof PaletteEditorWorkspace) {
			//Changed what we want to paint
			if (gameObjects.size == 1) {
				//We have something new to paint, make a new instance of the game object

				GameObject copied = AssetRepository.getInstance().copyGameObject(gameObjects.get(0));
				TileDataComponent tileDataComponent = copied.getComponent(TileDataComponent.class);
				TransformComponent transformComponent = copied.getComponent(TransformComponent.class);
				GridPosition bottomLeftParentTile = tileDataComponent.getBottomLeftParentTile();
				copied.getTransformSettings().setOffset(bottomLeftParentTile.x, bottomLeftParentTile.y);
				copied.getTransformSettings().setStoredTransformOffset(transformComponent.position);
				gameObjectWeArePainting = copied;
				gameObjectWeArePainting.isPlacing = true;
			} else if (gameObjects.size == 0) {
				gameObjectWeArePainting = null;
			}
		}

		if (mapComponent != null) {
			for (TalosLayer layer : mapComponent.getLayers()) {
				layer.entityPlacing = null;
			}
		}

		if (painting) {
			showDrawingObject();
		}
	}

	private void showDrawingObject () {
		if (layerSelected != null) {
			if (painting) {
				layerSelected.entityPlacing = gameObjectWeArePainting;
			}
		}
	}

	private void hideDrawingObject () {
		if (layerSelected != null) {
			if (painting) {
				layerSelected.entityPlacing = null;
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


		for (TalosLayer tLayer : mapComponent.getLayers()) {
			tLayer.entityPlacing = null;
		}
		if (layerSelected != null) {
			layerSelected.entityPlacing = gameObjectWeArePainting;
		}
	}

	private void focusMap (GameObject mapObject) {
		focusedMapObject = mapObject;
		mapComponent = mapObject.getComponent(MapComponent.class);
		mapFocused = true;
		showDrawingObject();
	}

	private void unfocusMap () {
		hideDrawingObject();

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

	public GameObject getGameObjectWeArePainting () {
		return gameObjectWeArePainting;
	}

	public TalosLayer getLayerSelected () {
		return layerSelected;
	}
}
