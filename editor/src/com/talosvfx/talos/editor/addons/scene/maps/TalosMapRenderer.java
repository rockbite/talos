package com.talosvfx.talos.editor.addons.scene.maps;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.editor.addons.scene.MainRenderer;
import com.talosvfx.talos.runtime.scene.GameObject;import com.talosvfx.talos.editor.addons.scene.logic.componentwrappers.MapComponent;
import com.talosvfx.talos.editor.addons.scene.logic.componentwrappers.TileDataComponent;
import com.talosvfx.talos.editor.addons.scene.logic.componentwrappers.TransformComponent;
import com.talosvfx.talos.runtime.maps.LayerType;
import com.talosvfx.talos.runtime.maps.MapType;
import com.talosvfx.talos.runtime.maps.StaticTile;
import com.talosvfx.talos.runtime.maps.TalosLayer;

import java.util.Comparator;

public class TalosMapRenderer {

	private Camera camera;

	public void setCamera (Camera camera) {
		this.camera = camera;
	}

	private interface RenderFunction {
		void render (MainRenderer mainRenderer, PolygonBatch batch, GameObject entityThatHasTheMap, MapComponent map);
	}

	private ObjectMap<MapType, RenderFunction> renderModes = new ObjectMap<>();


	private Comparator<GameObject> orthoTopDownSorter = new Comparator<GameObject>() {
		@Override
		public int compare (GameObject a, GameObject b) {

			//do z sorting on elements at the top level
			if (a.parent == null && b.parent == null) {
				TransformComponent ATransform = a.getComponent(TransformComponent.class);
				TransformComponent BTransform = b.getComponent(TransformComponent.class);

				float AworldPosY = ATransform.worldPosition.y;
				float BworldPosY = BTransform.worldPosition.y;

				if (a.hasComponent(TileDataComponent.class)) {
					AworldPosY += (a.getComponent(TileDataComponent.class).getFakeZ());
				}
				if (b.hasComponent(TileDataComponent.class)) {
					BworldPosY += (b.getComponent(TileDataComponent.class).getFakeZ());
				}

				return -Float.compare(AworldPosY, BworldPosY);
			} else {
				float aSort = MainRenderer.getDrawOrderSafe(a);
				float bSort = MainRenderer.getDrawOrderSafe(b);

				return Float.compare(aSort, bSort);
			}

		}
	};

	private MainRenderer.RenderState state;

	public TalosMapRenderer () {
		renderModes.put(MapType.ORTHOGRAPHIC_TOPDOWN, this::orthoRenderMap);
		state = new MainRenderer.RenderState();
	}

	private void orthoRenderMap (MainRenderer mainRenderer, PolygonBatch batch, GameObject gameObject, MapComponent map) {
		Array<TalosLayer> layers = map.getLayers();

		for (TalosLayer layer : layers) {
			//Find the type
			LayerType type = layer.getType();
			switch (type) {
			case STATIC:

				IntMap<IntMap<StaticTile>> staticTiles = layer.getStaticTiles();

				int mapWidth = layer.getMapWidth();
				int mapHeight = layer.getMapHeight();

				Vector3 position = camera.position;

				float zoom = 1f;
				if (camera instanceof OrthographicCamera) {
					zoom = ((OrthographicCamera)camera).zoom;
				}


				float viewportWidth = camera.viewportWidth * zoom;
				float viewportHeight = camera.viewportHeight * zoom;

				float startX = position.x - viewportWidth / 2;
				float startY = position.y + viewportHeight / 2;

				//Top down left to right

				int tileSizeX = MathUtils.round(layer.getTileSizeX());
				int tileSizeY = MathUtils.round(layer.getTileSizeY());

				if (tileSizeX == 0) {
					tileSizeX = 1;
				}
				if (tileSizeY == 0) {
					tileSizeY = 1;
				}

				int sX = (int)(MathUtils.floor(startX / tileSizeX) * tileSizeX);
				int sY = (int)(MathUtils.floor(startY / tileSizeY) * tileSizeY);

				for (int i = sX - 1; i < sX + viewportWidth + 1; i += tileSizeX) {
					if (i < 0 || i >= mapWidth) {
						continue;
					}

					if (staticTiles.containsKey(i)) {
						IntMap<StaticTile> entries = staticTiles.get(i);
						for (int j = sY + 1; j > sY - viewportHeight - 1; j -= tileSizeY) {
							if (j < 0 || j >= mapHeight) {
								continue;
							}

							if (entries.containsKey(j)) {
								StaticTile staticTile = entries.get(j);

								renderTileDynamic(mainRenderer, batch, staticTile, layer.getTileSizeX(), layer.getTileSizeY());

							}
						}
					}

				}

				break;
			case DYNAMIC_ENTITY:

				Array<GameObject> rootEntities = layer.getRootEntities();

				mainRenderer.setActiveSorter(orthoTopDownSorter);
				for (GameObject rootEntity : rootEntities) {
					mainRenderer.update(rootEntity);
				}

				Array<GameObject> temp = new Array<>();
				if (layer.entityPlacing != null) {
					mainRenderer.update(layer.entityPlacing);
					temp.add(layer.entityPlacing);
				}
				temp.addAll(rootEntities);

				mainRenderer.render(batch, state, temp);

				mainRenderer.setActiveSorter(mainRenderer.layerAndDrawOrderComparator);

				break;
			}
		}
	}

	public void renderTileDynamic (MainRenderer mainRenderer, Batch batch, StaticTile staticTile, float tileSizeX, float tileSizeY) {
		mainRenderer.renderStaticTileDynamic(staticTile, batch, tileSizeX, tileSizeY);
	}

	public void render (MainRenderer mainRenderer, PolygonBatch batch, GameObject entityThatHasTheMap, MapComponent map) {
		MapType mapType = map.getMapType();
		renderModes.get(mapType).render(mainRenderer, batch, entityThatHasTheMap, map);
	}

}
