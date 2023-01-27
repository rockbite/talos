package com.talosvfx.talos.runtime.scene;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonBatch;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.runtime.scene.components.MapComponent;
import com.talosvfx.talos.runtime.scene.components.ParticleComponent;
import com.talosvfx.talos.runtime.scene.components.RendererComponent;
import com.talosvfx.talos.runtime.scene.components.RoutineRendererComponent;
import com.talosvfx.talos.runtime.scene.components.SpineRendererComponent;
import com.talosvfx.talos.runtime.scene.components.SpriteRendererComponent;
import com.talosvfx.talos.runtime.scene.components.TransformComponent;
import com.talosvfx.talos.runtime.scene.render.ComponentRenderer;
import com.talosvfx.talos.runtime.scene.render.MapComponentRenderer;
import com.talosvfx.talos.runtime.scene.render.RenderState;
import com.talosvfx.talos.runtime.scene.render.RoutineComponentRenderer;
import com.talosvfx.talos.runtime.scene.render.SimpleParticleComponentRenderer;
import com.talosvfx.talos.runtime.scene.render.SkeletonComponentRenderer;
import com.talosvfx.talos.runtime.scene.render.SpriteComponentRenderer;

import java.util.Comparator;

public class GameObjectRenderer {

	public static SceneLayer DEFAULT_SCENE_LAYER = new SceneLayer("Default", 0);

	private ComponentRenderer<SpriteRendererComponent> spriteRenderer;
	private ComponentRenderer<MapComponent> mapRenderer;
	private ComponentRenderer<ParticleComponent<?>> particleRenderer;
	private ComponentRenderer<RoutineRendererComponent<?>> routineRenderer;
	private ComponentRenderer<SpineRendererComponent> spineRenderer;

	public final Comparator<GameObject> layerAndDrawOrderComparator;
	private Comparator<GameObject> activeSorter;

	private Camera camera;

	public GameObjectRenderer () {
		spriteRenderer = createSpriteRenderer();
		mapRenderer = createMapRenderer();
		particleRenderer = createParticleRenderer();
		routineRenderer = createRoutineRenderer();
		spineRenderer = createSpineRenderer();

		layerAndDrawOrderComparator = new Comparator<GameObject>() {
			@Override
			public int compare (GameObject o1, GameObject o2) {
				SceneLayer o1Layer = GameObjectRenderer.getLayerSafe(o1);
				SceneLayer o2Layer = GameObjectRenderer.getLayerSafe(o2);

				if (o1Layer.equals(o2Layer)) {
					float aSort = GameObjectRenderer.getDrawOrderSafe(o1);
					float bSort = GameObjectRenderer.getDrawOrderSafe(o2);
					return Float.compare(aSort, bSort);
				} else {
					return Integer.compare(o1Layer.getIndex(), o2Layer.getIndex());
				}
			}
		};

		activeSorter = layerAndDrawOrderComparator;
	}

	private static SceneLayer getLayerSafe(GameObject gameObject) {
		if (gameObject.hasComponentType(RendererComponent.class)) {
			RendererComponent rendererComponent = gameObject.getComponentAssignableFrom(RendererComponent.class);
			return rendererComponent.sortingLayer;
		}
		return DEFAULT_SCENE_LAYER;
	}

	public static float getDrawOrderSafe (GameObject gameObject) {
		if (gameObject.hasComponentType(RendererComponent.class)) {
			RendererComponent rendererComponent = gameObject.getComponentAssignableFrom(RendererComponent.class);
			return rendererComponent.orderingInLayer;
		}
		return -55;
	}
	public void setActiveSorter (Comparator<GameObject> customSorter) {
		this.activeSorter = customSorter;
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

	private void sort (Array<GameObject> list) {
		list.sort(activeSorter);
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

	private void fillRenderableEntities (Array<GameObject> rootObjects, Array<GameObject> list) {
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
		if (gameObject.hasComponent(SpriteRendererComponent.class)) {
			spriteRenderer.render(batch, camera, gameObject, gameObject.getComponent(SpriteRendererComponent.class));
		} else if (gameObject.hasComponent(ParticleComponent.class)) {
			particleRenderer.render(batch,camera,  gameObject, gameObject.getComponent(ParticleComponent.class));
		} else if (gameObject.hasComponent(SpineRendererComponent.class)) {
			spineRenderer.render(batch, camera, gameObject, gameObject.getComponent(SpineRendererComponent.class));
		} else if (gameObject.hasComponent(MapComponent.class)) {
			mapRenderer.render(batch, camera, gameObject, gameObject.getComponent(MapComponent.class));
		} else if (gameObject.hasComponent(RoutineRendererComponent.class)) {
			routineRenderer.render(batch, camera, gameObject, gameObject.getComponent(RoutineRendererComponent.class));
		}
	}
	public void buildRenderStateAndRender (PolygonBatch batch, RenderState state, GameObject root) {
		temp.clear();
		temp.add(root);
		buildRenderStateAndRender(batch, state, temp);
	}
	public void buildRenderStateAndRender (PolygonBatch batch, RenderState state, Array<GameObject> rootObjects) {
		buildRenderState(batch, state, rootObjects);
		for (int i = 0; i < state.list.size; i++) {
			GameObject gameObject = state.list.get(i);
			renderObject(batch, gameObject);
		}
	}

	public void setCamera (Camera camera) {
		this.camera = camera;
	}
}
