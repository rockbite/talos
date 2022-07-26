package com.talosvfx.talos.editor.addons.scene.maps;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.TalosMain;
import com.talosvfx.talos.editor.addons.scene.MainRenderer;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.MapComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.RendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;

import java.util.Comparator;

public class TalosMapRenderer {

	private OrthographicCamera camera;

	public void setCamera (OrthographicCamera camera) {
		this.camera = camera;
	}

	private interface RenderFunction {
		void render (MainRenderer mainRenderer, Batch batch, GameObject entityThatHasTheMap, MapComponent map);
	}
	private ObjectMap<MapType, RenderFunction> renderModes = new ObjectMap<>();

	private Comparator<GameObject> orthoTopDownSorter = new Comparator<GameObject>() {
		@Override
		public int compare (GameObject o1, GameObject o2) {

			TransformComponent o1c = o1.getComponent(TransformComponent.class);
			TransformComponent o2c = o2.getComponent(TransformComponent.class);

			//Y only

			return Float.compare(o1c.position.y, o2c.position.y);
		}
	};

	public TalosMapRenderer () {
		renderModes.put(MapType.ORTHOGRAPHIC_TOPDOWN, this::orthoRenderMap);
	}

	private void orthoRenderMap (MainRenderer mainRenderer, Batch batch, GameObject gameObject, MapComponent map) {
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
				float zoom = camera.zoom;
				float viewportWidth = camera.viewportWidth * zoom;
				float viewportHeight = camera.viewportHeight * zoom;

				float startX = position.x - viewportWidth/2;
				float startY = position.y + viewportHeight/2;

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

				for (int i = sX; i < sX + viewportWidth; i += tileSizeX) {
					if (i < 0 || i >= mapWidth) {
						continue;
					}

					if (staticTiles.containsKey(i)) {
						IntMap<StaticTile> entries = staticTiles.get(i);
						for (int j = sY; j > sY - viewportHeight; j -= tileSizeY) {
							if (j < 0 || j >= mapHeight) {
								continue;
							}

							if (entries.containsKey(j)) {
								StaticTile staticTile = entries.get(j);

								renderTileAt(i, j, staticTile);

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
					mainRenderer.render(batch, rootEntity);
				}

				break;
			}
		}
	}

	private void renderTileAt (int i, int j, StaticTile staticTile) {
	}

	public void render (MainRenderer mainRenderer, Batch batch, GameObject entityThatHasTheMap, MapComponent map) {
		MapType mapType = map.getMapType();
		renderModes.get(mapType).render(mainRenderer, batch, entityThatHasTheMap, map);
	}


}
