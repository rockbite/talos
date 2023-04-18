package com.talosvfx.talos.runtime.scene;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.talosvfx.talos.runtime.scene.components.*;
import com.talosvfx.talos.runtime.scene.render.*;
import com.talosvfx.talos.runtime.RuntimeContext;
import lombok.Getter;

import java.util.Comparator;

public class GameObjectRenderer {
	private ComponentRenderer<SpriteRendererComponent> spriteRenderer;
	private ComponentRenderer<MapComponent> mapRenderer;
	private ComponentRenderer<ParticleComponent<?>> particleRenderer;
	private ComponentRenderer<RoutineRendererComponent<?>> routineRenderer;
	private ComponentRenderer<SpineRendererComponent> spineRenderer;
	private ComponentRenderer<PathRendererComponent> pathRenderer;

	public final Comparator<GameObject> layerAndDrawOrderComparator;
	public final Comparator<GameObject> yDownDrawOrderComparator;

	public final Comparator<GameObject> parentSorter;

	private Camera camera;

	@Getter
	private boolean skipUpdates;

	public GameObjectRenderer () {
		spriteRenderer = createSpriteRenderer();
		mapRenderer = createMapRenderer();
		particleRenderer = createParticleRenderer();
		routineRenderer = createRoutineRenderer();
		spineRenderer = createSpineRenderer();
		pathRenderer = createPathRenderer();

		layerAndDrawOrderComparator = new Comparator<GameObject>() {
			@Override
			public int compare (GameObject o1, GameObject o2) {
				float aSort = GameObjectRenderer.getDrawOrderSafe(o1);
				float bSort = GameObjectRenderer.getDrawOrderSafe(o2);
				return Float.compare(aSort, bSort);
			}
		};

		yDownDrawOrderComparator = new Comparator<GameObject>() {
			@Override
			public int compare (GameObject o1, GameObject o2) {
				float aSort = GameObjectRenderer.getBottomY(o1);
				float bSort = GameObjectRenderer.getBottomY(o2);
				return -Float.compare(aSort, bSort);
			}
		};

		parentSorter = new Comparator<GameObject>() {
			@Override
			public int compare (GameObject o1, GameObject o2) {
				SceneLayer o1Layer = GameObjectRenderer.getLayerSafe(o1);
				SceneLayer o2Layer = GameObjectRenderer.getLayerSafe(o2);

				if (o1Layer.equals(o2Layer)) {

					RenderStrategy renderStrategy = o1Layer.getRenderStrategy();


					Comparator<GameObject> sorter = getSorter(renderStrategy);

					return sorter.compare(o1, o2);
				} else {
					return Integer.compare(o1Layer.getIndex(), o2Layer.getIndex());
				}
			}
		};

	}
	private Comparator<GameObject> getSorter (RenderStrategy renderMode) {
		switch (renderMode) {
		case SCENE:
			return layerAndDrawOrderComparator;
		case YDOWN:
			return yDownDrawOrderComparator;
		}

		throw new GdxRuntimeException("No sorter found for render mode: " + renderMode);
	}
	private static float getBottomY (GameObject gameObject) {
		if (gameObject.hasComponentType(RendererComponent.class)) {
			RendererComponent componentAssignableFrom = gameObject.getComponentAssignableFrom(RendererComponent.class);
			TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);

			float y = transformComponent.worldPosition.y;


			if (componentAssignableFrom instanceof SpriteRendererComponent) {
				Vector2 size = ((SpriteRendererComponent)componentAssignableFrom).size;
				Vector2 worldScale = transformComponent.worldScale;

				float totalHeight = size.y * worldScale.y;
				y -= totalHeight/2f;
			}

			if (componentAssignableFrom instanceof RendererComponent) {
				float fakeOffsetY = componentAssignableFrom.fakeOffsetY;
				y += fakeOffsetY;
			}

			return y;

		} else {
			if (gameObject.hasComponent(TransformComponent.class)) {
				TransformComponent component = gameObject.getComponent(TransformComponent.class);
				return component.worldPosition.y;
			}
		}

		return 0;
	}

	private static SceneLayer getLayerSafe(GameObject gameObject) {
		if (gameObject.hasComponentType(RendererComponent.class)) {
			RendererComponent rendererComponent = gameObject.getComponentAssignableFrom(RendererComponent.class);
			return rendererComponent.sortingLayer;
		}

		return RuntimeContext.getInstance().sceneData.getPreferredSceneLayer();
	}

	public static float getDrawOrderSafe (GameObject gameObject) {
		if (gameObject.hasComponentType(RendererComponent.class)) {
			RendererComponent rendererComponent = gameObject.getComponentAssignableFrom(RendererComponent.class);
			return rendererComponent.orderingInLayer;
		}

		return -55;
	}


	protected ComponentRenderer<MapComponent> createMapRenderer () {
		return new MapComponentRenderer(this);
	}

	protected ComponentRenderer<RoutineRendererComponent<?>> createRoutineRenderer () {
		return new RoutineComponentRenderer(this);
	}

	protected ComponentRenderer<SpineRendererComponent> createSpineRenderer () {
		return new SkeletonComponentRenderer(this);
	}

	protected ComponentRenderer<ParticleComponent<?>> createParticleRenderer () {
		return new SimpleParticleComponentRenderer(this);
	}

	protected ComponentRenderer<SpriteRendererComponent> createSpriteRenderer () {
		return new SpriteComponentRenderer(this);
	}

	protected ComponentRenderer<PathRendererComponent> createPathRenderer () {
		return new PathComponentRenderer(this);
	}

	protected void sort (Array<GameObject> list) {
		list.sort(parentSorter);
	}

	public void update (GameObject gameObject, float delta) {
		if (!gameObject.active || !gameObject.isEditorVisible())
			return;
		if (gameObject.hasComponent(TransformComponent.class)) {
			TransformComponent transform = gameObject.getComponent(TransformComponent.class);

			transform.worldPosition.set(transform.position);
			transform.worldScale.set(transform.scale);
			transform.worldRotation = transform.rotation;

			if (gameObject.parent != null) {

				if (gameObject.parent.hasComponent(TransformComponent.class)) {
					//Combine our world with the parent

					TransformComponent parentTransform = gameObject.parent.getComponent(TransformComponent.class);
					transform.worldPosition.scl(parentTransform.worldScale);
					transform.worldPosition.rotateDeg(parentTransform.worldRotation);
					transform.worldPosition.add(parentTransform.worldPosition);

					transform.worldRotation += parentTransform.worldRotation;
					transform.worldScale.scl(parentTransform.worldScale);
				}
			}
		}

		// if root has render component try mixing colors if they exist
		if (gameObject.hasComponentType(RendererComponent.class)) {
			final RendererComponent rendererComponent = gameObject.getComponentAssignableFrom(RendererComponent.class);

			// check if render component has color value
			if (rendererComponent instanceof IColorHolder) {
				final IColorHolder colorHolder = (IColorHolder)rendererComponent;

				// update final color by Renderer color
				final Color finalColor = (colorHolder.getFinalColor());
				finalColor.set(colorHolder.getColor());

				// should inherit parent color update final color by parent color
				if (colorHolder.shouldInheritParentColor()) {
					if (gameObject.parent != null) {
						// check if parent contains render component
						if (gameObject.parent.hasComponentType(RendererComponent.class)) {
							final RendererComponent parentRendererComponent = gameObject.parent.getComponentAssignableFrom(RendererComponent.class);

							// check if parent render component has color value
							if (parentRendererComponent instanceof IColorHolder) {
								// combine colors
								finalColor.mul(((IColorHolder)parentRendererComponent).getFinalColor());
							}
						}
					}
				}
			}
		}

		if (gameObject.getGameObjects() != null) {
			for (int i = 0; i < gameObject.getGameObjects().size; i++) {
				GameObject child = gameObject.getGameObjects().get(i);
				update(child, delta);
			}
		}
	}

	protected void fillRenderableEntities (Array<GameObject> rootObjects, Array<GameObject> list) {
		for (GameObject root : rootObjects) {
			if (!root.active || !root.isEditorVisible()) continue;

			boolean childrenVisibleFlag = true;
			if (root.hasComponentType(RendererComponent.class)) {
				RendererComponent rendererComponent = root.getComponentAssignableFrom(RendererComponent.class);
				childrenVisibleFlag = rendererComponent.childrenVisible;
				if (rendererComponent.visible) {
					list.add(root);
				}
			}
			if (childrenVisibleFlag) {
				if (root.getGameObjects() != null) {
					fillRenderableEntities(root.getGameObjects(), list);
				}
			}
		}

	}

	Array<GameObject> temp = new Array<>();
	public void buildRenderState (PolygonBatch batch, RenderState state, GameObject root) {
		temp.clear();
		temp.add(root);
		buildRenderState(batch, state, temp);
	}
	public void buildRenderState (PolygonBatch batch, RenderState state, Array<GameObject> rootObjects) {
		state.list.clear();
		fillRenderableEntities(rootObjects, state.list);
		sort(state.list);
	}


	public void renderObject (Batch batch, GameObject gameObject) {
		if (gameObject.hasComponent(RoutineRendererComponent.class)) {
			routineRenderer.render(batch, camera, gameObject, gameObject.getComponent(RoutineRendererComponent.class));
		}

		if (gameObject.hasComponent(SpriteRendererComponent.class)) {
			spriteRenderer.render(batch, camera, gameObject, gameObject.getComponent(SpriteRendererComponent.class));
		} else if (gameObject.hasComponent(ParticleComponent.class)) {
			particleRenderer.render(batch,camera,  gameObject, gameObject.getComponent(ParticleComponent.class));
		} else if (gameObject.hasComponent(SpineRendererComponent.class)) {
			spineRenderer.render(batch, camera, gameObject, gameObject.getComponent(SpineRendererComponent.class));
		} else if (gameObject.hasComponent(MapComponent.class)) {
			mapRenderer.render(batch, camera, gameObject, gameObject.getComponent(MapComponent.class));
		} else if (gameObject.hasComponent(PathRendererComponent.class)) {
			pathRenderer.render(batch, camera, gameObject, gameObject.getComponent(PathRendererComponent.class));
		}

	}
	public void buildRenderStateAndRender (PolygonBatch batch, Camera camera, RenderState state, GameObject root) {
		temp.clear();
		temp.add(root);
		buildRenderStateAndRender(batch, camera, state, temp);
	}
	public void buildRenderStateAndRender (PolygonBatch batch, Camera camera, RenderState state, Array<GameObject> rootObjects) {
		setCamera(camera);

		buildRenderState(batch, state, rootObjects);
		for (int i = 0; i < state.list.size; i++) {
			GameObject gameObject = state.list.get(i);
			renderObject(batch, gameObject);
		}
	}

	public void setCamera (Camera camera) {
		this.camera = camera;
	}

	/**
	 * Any renderers that may want to skip updates do it here
	 * @param skipUpdates
	 */
	public void setSkipUpdates (boolean skipUpdates) {
		this.skipUpdates = skipUpdates;
	}
}
