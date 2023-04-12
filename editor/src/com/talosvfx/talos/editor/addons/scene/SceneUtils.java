package com.talosvfx.talos.editor.addons.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.esotericsoftware.spine.SkeletonData;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.runtime.RuntimeContext;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.editor.addons.scene.events.*;
import com.talosvfx.talos.editor.addons.scene.events.scene.DeSelectGameObjectExternallyEvent;
import com.talosvfx.talos.editor.addons.scene.events.scene.SelectGameObjectExternallyEvent;
import com.talosvfx.talos.runtime.assets.meta.SpriteMetadata;
import com.talosvfx.talos.runtime.maps.TalosLayer;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.events.DirectoryChangedEvent;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.project2.TalosProjectData;
import com.talosvfx.talos.editor.project2.apps.ProjectExplorerApp;
import com.talosvfx.talos.editor.project2.apps.SceneEditorApp;
import com.talosvfx.talos.editor.serialization.VFXProjectData;
import com.talosvfx.talos.runtime.scene.components.*;
import com.talosvfx.talos.runtime.utils.NamingUtils;
import com.talosvfx.talos.editor.utils.Toasts;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.GameObjectContainer;
import com.talosvfx.talos.runtime.scene.Prefab;
import com.talosvfx.talos.runtime.scene.SavableContainer;
import com.talosvfx.talos.runtime.scene.Scene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter.fromDirectoryView;
import static com.talosvfx.talos.editor.addons.scene.widgets.gizmos.SmartTransformGizmo.getLatestFreeOrderingIndex;

public class SceneUtils {

	private static final Logger logger = LoggerFactory.getLogger(SceneUtils.class);

	public static GameObject createEmpty (GameObjectContainer gameObjectContainer, Vector2 position, GameObject parent) {
		GameObject emptyObject =  createObjectByTypeName(gameObjectContainer, "empty", position, parent, "empty");

		Notifications.fireEvent(Notifications.obtainEvent(SelectGameObjectExternallyEvent.class).setGameObject(emptyObject));

		return emptyObject;
	}

	public static GameObject createSpriteObject (GameObjectContainer gameObjectContainer, GameAsset<AtlasRegion> spriteAsset, Vector2 sceneCords, GameObject parent) {
		GameObject spriteObject = createObjectByTypeName(gameObjectContainer, "sprite", sceneCords, parent, spriteAsset.nameIdentifier);
		SpriteRendererComponent component = spriteObject.getComponent(SpriteRendererComponent.class);

        AtlasRegion texture = spriteAsset.getResource();
		SpriteMetadata metaData = (SpriteMetadata)spriteAsset.getRootRawAsset().metaData;
        component.size.x = texture.getRegionWidth() / metaData.pixelsPerUnit;
        component.size.y = texture.getRegionHeight() / metaData.pixelsPerUnit;

		if (!fromDirectoryView) {
			component.orderingInLayer = getLatestFreeOrderingIndex(gameObjectContainer, component.sortingLayer);
		}
		component.setGameAsset(spriteAsset);

		Notifications.fireEvent(Notifications.obtainEvent(SelectGameObjectExternallyEvent.class).setGameObject(spriteObject));

		return spriteObject;
	}

	public static GameObject createSpineObject (GameObjectContainer gameObjectContainer, GameAsset<SkeletonData> asset, Vector2 sceneCords, GameObject parent) {
		GameObject spineObject = createObjectByTypeName(gameObjectContainer, "spine", sceneCords, parent, asset.nameIdentifier);
		SpineRendererComponent rendererComponent = spineObject.getComponent(SpineRendererComponent.class);

		if (!fromDirectoryView) {
			rendererComponent.orderingInLayer = getLatestFreeOrderingIndex(gameObjectContainer, rendererComponent.sortingLayer);
		}
		rendererComponent.setGameAsset(asset);

		Notifications.fireEvent(Notifications.obtainEvent(SelectGameObjectExternallyEvent.class).setGameObject(spineObject));

		return spineObject;
	}

	public static GameObject createParticle (GameObjectContainer gameObjectContainer, GameAsset<VFXProjectData> asset, Vector2 sceneCords, GameObject parent) {
		GameObject particleObject = createObjectByTypeName(gameObjectContainer, "particle", sceneCords, parent, asset.nameIdentifier);
		ParticleComponent component = particleObject.getComponent(ParticleComponent.class);

		if (!fromDirectoryView) {
			component.orderingInLayer = getLatestFreeOrderingIndex(gameObjectContainer, component.sortingLayer);
		}
		component.setGameAsset(asset);

		Notifications.fireEvent(Notifications.obtainEvent(SelectGameObjectExternallyEvent.class).setGameObject(particleObject));

		return particleObject;
	}

	public static GameObject createFromPrefab (GameObjectContainer gameObjectContainer, GameAsset<Prefab> prefabToCopy, Vector2 position, GameObject parent) {

		Prefab prefab = new Prefab(prefabToCopy.getRootRawAsset().handle);

		GameObject gameObject = prefab.root;
		TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);
		transformComponent.position.add(position);

		String name = NamingUtils.getNewName(prefab.name, gameObjectContainer.getAllGONames());

		gameObject.setName(name);

		randomizeChildrenUUID(gameObject);
		if (parent == null) {
			gameObjectContainer.addGameObject(gameObject);
		} else {
			parent.addGameObject(gameObject);
		}


		onObjectCreated(gameObjectContainer, gameObject);

		Notifications.fireEvent(Notifications.obtainEvent(SelectGameObjectExternallyEvent.class).setGameObject(gameObject));

		return gameObject;
	}

	private static void onObjectCreated (GameObjectContainer gameObjectContainer, GameObject gameObject) {
		Notifications.fireEvent(Notifications.obtainEvent(GameObjectCreated.class).set(gameObjectContainer, gameObject));
		markContainerChanged(gameObjectContainer);
	}

	public static GameObject createObjectByTypeName (GameObjectContainer gameObjectContainer, String idName, Vector2 position, GameObject parent, String nameHint) {
		GameObject gameObject = new GameObject();

		XmlReader.Element template = RuntimeContext.getInstance().configData.getGameObjectConfigurationMap().get(idName);

		String nameAttribute = template.getAttribute("nameTemplate", "gameObject");

		String name = NamingUtils.getNewName(nameHint, gameObjectContainer.getAllGONames());

		gameObject.setName(name);
		initComponentsFromTemplate(gameObject, template);
		initializeDefaultValues(gameObject, position);

		if (parent == null) {
			gameObjectContainer.addGameObject(gameObject);
		} else {
			parent.addGameObject(gameObject);
		}

		onObjectCreated(gameObjectContainer, gameObject);
		return gameObject;
	}

	private static void initComponentsFromTemplate (GameObject gameObject, XmlReader.Element template) {
		XmlReader.Element container = template.getChildByName("components");
		Array<XmlReader.Element> componentsXMLArray = container.getChildrenByName("component");
		for (XmlReader.Element componentXML : componentsXMLArray) {
			String className = componentXML.getAttribute("className");
			String classPath = RuntimeContext.getInstance().configData.getComponentClassPath();

			try {
				Class clazz = ClassReflection.forName(classPath + "." + className);
				Object instance = ClassReflection.newInstance(clazz);
				AComponent component = (AComponent)instance;
				// TOM SAYS TO DO THIS IT"S NOT ME I SWEAR
				Json json = new Json();
				String s = json.toJson(component);
				component = json.fromJson(component.getClass(), s);
				gameObject.addComponent(component);

				if (component instanceof MapComponent) {
					//Add a layer also
					TalosLayer layer = new TalosLayer("NewLayer");
					((MapComponent)component).getLayers().add(layer);
				}
				if (component instanceof CurveComponent) {
					final SceneEditorApp currentApp = SharedResources.appManager.getSingletonAppInstance(SceneEditorApp.class);
					final Supplier<Camera> currentCameraSupplier = currentApp.getWorkspaceWidget().getViewportViewSettings().getCurrentCameraSupplier();
					((CurveComponent) component).scale(currentCameraSupplier.get().viewportWidth / 10);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static void initializeDefaultValues (GameObject gameObject, Vector2 position) {
		if (position != null && gameObject.hasComponent(TransformComponent.class)) {
			// oh boi always special case with this fuckers
			TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);
			transformComponent.position.set(position.x, position.y);
		}

		if (gameObject.hasComponent(ParticleComponent.class)) {
			GameAsset<VFXProjectData> sample = AssetRepository.getInstance().getAssetForIdentifier("sample", GameAssetType.VFX);
			if (sample.isBroken()) {
				// first time for sample
				FileHandle vfx = SharedResources.currentProject.rootProjectDir().child("vfx");
				if (!vfx.exists()) {
					vfx.mkdirs();
				}
				AssetRepository.getInstance().copySampleParticleToProject(vfx);
				sample = AssetRepository.getInstance().getAssetForIdentifier("sample", GameAssetType.VFX);
			}
			ParticleComponent particleComponent = gameObject.getComponent(ParticleComponent.class);
			particleComponent.setGameAsset(sample);
		}
	}

	public static void randomizeChildrenUUID (GameObject parent) {
		parent.uuid = UUID.randomUUID();
		Array<GameObject> gameObjects = parent.getGameObjects();
		if (gameObjects != null) {
			for (GameObject gameObject : gameObjects) {
				randomizeChildrenUUID(gameObject);
			}
		}
	}

	public static void repositionGameObject (GameObjectContainer currentContainer, GameObject parentToMoveTo, GameObject childThatHasMoved) {
		if (parentToMoveTo == null) {
			parentToMoveTo = currentContainer.getSelfObject();
		}

		if (childThatHasMoved.parent != null) {
			childThatHasMoved.parent.removeObject(childThatHasMoved);
		}

		parentToMoveTo.addGameObject(childThatHasMoved);
		GameObject.projectInParentSpace(parentToMoveTo, childThatHasMoved);
		//for updating left panel values

		Notifications.fireEvent(Notifications.obtainEvent(SelectGameObjectExternallyEvent.class).setGameObject(childThatHasMoved));

		markContainerChanged(currentContainer);

	}


	public static void deleteGameObject (GameObjectContainer gameObjectContainer, GameObject gameObject) {
		DeSelectGameObjectExternallyEvent deSelectGameObjectExternallyEvent = Notifications.obtainEvent(DeSelectGameObjectExternallyEvent.class);
		deSelectGameObjectExternallyEvent.setGameObject(gameObject);
		Notifications.fireEvent(deSelectGameObjectExternallyEvent);

		GameObjectDeleted gameObjectDeleted = Notifications.obtainEvent(GameObjectDeleted.class);
		gameObjectDeleted.set(gameObjectContainer, gameObject);
		Notifications.fireEvent(gameObjectDeleted);

		markContainerChanged(gameObjectContainer);

	}

	private static ObjectMap<GameObjectContainer, OrderedSet<GameObject>> copyPasteBuffer = new ObjectMap<>();

	public static void copy (GameAsset<Scene> gameAsset, OrderedSet<GameObject> selection) {
		final GameObjectContainer currentContainer = gameAsset.getResource();

		copyPasteBuffer.put(currentContainer, selection);

		final Vector3 camPos = getCameraPosForScene(gameAsset);

		SceneEditorWorkspace.ClipboardPayload payload = new SceneEditorWorkspace.ClipboardPayload();
		Array<GameObject> gameObjects = selection.orderedItems();
		for (int i = 0; i < selection.size; i++) {
			GameObject value = gameObjects.get(i);
			payload.objects.add(value);
			if (value.hasComponentType(TransformComponent.class)) {
				payload.objectWorldPositions.add(value.getComponent(TransformComponent.class).worldPosition);
			} else {
				payload.objectWorldPositions.add(new Vector2());
			}
		}

		payload.cameraPositionAtCopy.set(camPos.x, camPos.y);

		Json json = new Json();
		String clipboard = json.toJson(payload);
		Gdx.app.getClipboard().setContents(clipboard);
	}

	public static void paste (GameAsset<Scene> gameAsset) {
		final SavableContainer currentContainer = gameAsset.getResource();

		if (!copyPasteBuffer.containsKey(currentContainer)) return;

		final Vector3 camPosAtPaste = getCameraPosForScene(gameAsset);

		final OrderedSet<GameObject> selection = copyPasteBuffer.get(currentContainer);

		final String clipboard = Gdx.app.getClipboard().getContents();
		final Json json = new Json();

		try {
			final SceneEditorWorkspace.ClipboardPayload payload = json.fromJson(SceneEditorWorkspace.ClipboardPayload.class, clipboard);

			Vector2 offset = new Vector2(camPosAtPaste.x, camPosAtPaste.y);
			offset.sub(payload.cameraPositionAtCopy);

			GameObject parent = currentContainer.root;
			if (selection.size == 1 && selection.first() != currentContainer.root) {
				parent = selection.first().parent;
			}

			clearSelection(currentContainer);

			for (int i = 0; i < payload.objects.size; i++) {
				GameObject gameObject = payload.objects.get(i);

				String name = NamingUtils.getNewName(gameObject.getName(), currentContainer.getAllGONames());

				gameObject.setName(name);
				randomizeChildrenUUID(gameObject);
				parent.addGameObject(gameObject);
				if (gameObject.hasComponentType(TransformComponent.class)) {
					TransformComponent component = gameObject.getComponent(TransformComponent.class);
					component.worldPosition.set(payload.objectWorldPositions.get(i));
					GameObject.projectInParentSpace(parent, gameObject);
					component.position.add(offset);
				}

				if (!hierarchicallyContains(selection, gameObject)) {
					selection.add(gameObject);
				}

				Notifications.fireEvent(Notifications.obtainEvent(GameObjectCreated.class).set(currentContainer, gameObject));
			}
			Notifications.fireEvent(Notifications.obtainEvent(GameObjectSelectionChanged.class).set(currentContainer, selection));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param gameAsset scene game asset
	 * @return scene camera position if scene open else a zero vector
	 */
	private static Vector3 getCameraPosForScene (GameAsset<Scene> gameAsset) {
		final SceneEditorApp currentApp = SharedResources.appManager.getAppForAsset(SceneEditorApp.class, gameAsset);
		Vector3 camPos = new Vector3();
		if (currentApp != null) {
			final Supplier<Camera> currentCameraSupplier = currentApp.getWorkspaceWidget().getViewportViewSettings().getCurrentCameraSupplier();
			final Camera camera = currentCameraSupplier.get();
			camPos = camera.position;
		}
		return camPos;
	}

	public static void clearSelection (GameObjectContainer container) {
		copyPasteBuffer.get(container).clear();
	}

	// checks if gameObject or its ancestors are already in the selection or not
	public static boolean hierarchicallyContains (ObjectSet<GameObject> selection, GameObject gameObject) {
		GameObject temp = gameObject;
		while (temp != null) {
			if (selection.contains(temp)) return true;
			temp = temp.parent;
		}
		return false;
	}

	public static void componentAdded (GameObjectContainer currentHolder, GameObject gameObject, AComponent component) {
		ComponentAdded componentAdded = Notifications.obtainEvent(ComponentAdded.class);
		componentAdded.setContainer(currentHolder);
		componentAdded.setParent(gameObject);
		componentAdded.setComponent(component);
		Notifications.fireEvent(componentAdded);

		markContainerChanged(currentHolder);
	}

	public static void componentRemoved (GameObjectContainer currentHolder, GameObject gameObject, AComponent component) {
		ComponentRemoved componentRemoved = Notifications.obtainEvent(ComponentRemoved.class);
		componentRemoved.setContainer(currentHolder);
		componentRemoved.setGameObject(gameObject);
		componentRemoved.setComponent(component);
		Notifications.fireEvent(componentRemoved);

		markContainerChanged(currentHolder);
	}

	public static void componentUpdated (GameObjectContainer gameObjectContainer, GameObject gameObject, AComponent component) {
		componentUpdated(gameObjectContainer, gameObject, component, false);
	}

	public static void componentUpdated (GameObjectContainer gameObjectContainer, GameObject gameObject, AComponent component, boolean isRapid) {
		fireComponentUpdateEvent(gameObjectContainer, gameObject, component,isRapid);

		if (!isRapid) {
			markContainerChanged(gameObjectContainer);
		}
	}

	public static void componentBatchUpdated (GameObjectContainer gameObjectContainer, Array<GameObject> gameObjects, Class<? extends AComponent> componentType, boolean isRapid) {
		for (GameObject gameObject : gameObjects) {
			AComponent component = gameObject.getComponent(componentType);
			fireComponentUpdateEvent(gameObjectContainer, gameObject, component,isRapid);
		}

		if (!isRapid) {
			markContainerChanged(gameObjectContainer);
		}
	}

	public static void fireComponentUpdateEvent(GameObjectContainer gameObjectContainer, GameObject gameObject, AComponent component, boolean isRapid){
		ComponentUpdated componentUpdated = Notifications.obtainEvent(ComponentUpdated.class);
		componentUpdated.setComponent(component);
		componentUpdated.setParent(gameObject);
		componentUpdated.setRapid(isRapid);
		componentUpdated.setContainer(gameObjectContainer);
		Notifications.fireEvent(componentUpdated);
	}

	public static void fireComponentUpdateEvent(AComponent component, boolean isRapid){
		ComponentUpdated componentUpdated = Notifications.obtainEvent(ComponentUpdated.class);
		componentUpdated.setComponent(component);
		componentUpdated.setParent(component.getGameObject());
		componentUpdated.setRapid(isRapid);
		componentUpdated.setContainer(component.getGameObject().getGameObjectContainerRoot());
		Notifications.fireEvent(componentUpdated);
	}

	public static void layersUpdated () {
		TalosProjectData currentProject = SharedResources.currentProject;
		currentProject.save();
		Notifications.fireEvent(Notifications.obtainEvent(LayerListUpdated.class));
	}

	public static void markContainerChanged (GameObjectContainer currentHolder) {
		if (currentHolder instanceof Scene) {
			GameAsset<Scene> sceneGameAsset = AssetRepository.getInstance().getAssetForResource((Scene)currentHolder);
			if (sceneGameAsset != null) {
				AssetRepository.getInstance().assetChanged(sceneGameAsset);
			} else {
				logger.error("Couldn't find game asset for resource {}", currentHolder);
				Toasts.getInstance().showErrorToast("Couldn't save scene");
			}
		} else if (currentHolder instanceof Prefab) {
			GameAsset<Prefab> prefabGameAsset = AssetRepository.getInstance().getAssetForResource((Prefab)currentHolder);
			if (prefabGameAsset != null) {
				AssetRepository.getInstance().assetChanged(prefabGameAsset);
			} else {
				logger.error("Couldn't find game asset for resource {}", currentHolder);
				Toasts.getInstance().showErrorToast("Couldn't save prefab");
			}
		} else if (currentHolder instanceof GameObject) {
			//We need to find the scene that this game object belongs to, and get the game asset for that and save it

			GameObjectContainer gameObjectContainerRoot = ((GameObject)currentHolder).getGameObjectContainerRoot();
			markContainerChanged(gameObjectContainerRoot);
		} else {
			logger.info("Not something we can save");
		}
	}

	public static FileHandle getContextualFolderToCreateFile() {
		FileHandle assetDir = null;
		ProjectExplorerApp projectExplorerApp = SharedResources.appManager.getSingletonAppInstance(ProjectExplorerApp.class);
		if(projectExplorerApp != null) {
			assetDir = projectExplorerApp.getCurrentSelectedFolder();
		}
		if(assetDir == null) {
			assetDir = SharedResources.currentProject.rootProjectDir();
		}

		return assetDir;
	}

	public static void convertToPrefab (GameObject gameObject) {
		FileHandle root = SharedResources.currentProject.rootProjectDir();

		FileHandle prefabs = getContextualFolderToCreateFile();

		FileHandle[] children = prefabs.list();
		List<String> names = Arrays.stream(children).map((FileHandle child) -> child.name()).collect(Collectors.toList());
		String name = gameObject.getName() + ".prefab";
		String newName = NamingUtils.getNewName(name, names);
		FileHandle prefabHandle = prefabs.child(newName);

		gameObject.setName("Prefab_" + gameObject.getName());

		Prefab tempPrefabForSerialization = new Prefab(gameObject);
		String serializedPrefab = tempPrefabForSerialization.getAsString();

		Prefab newPrefab = new Prefab(serializedPrefab, gameObject.getName());

		if (newPrefab.getSelfObject().hasComponent(TransformComponent.class)) {
			TransformComponent component = newPrefab.getSelfObject().getComponent(TransformComponent.class);
			component.position.setZero();
		}

		prefabHandle.writeString(newPrefab.getAsString(), false);
		AssetRepository.getInstance().rawAssetCreated(prefabHandle, true);

		Notifications.fireEvent(Notifications.obtainEvent(DirectoryChangedEvent.class).set(prefabs.path()));
	}

	public static void visibilityUpdated (GameObjectContainer gameObjectContainer, GameObject gameObject) {
		Notifications.fireEvent(Notifications.obtainEvent(GameObjectVisibilityChanged.class).set(gameObjectContainer, gameObject));
		markContainerChanged(gameObjectContainer);
	}

	public static void lockUpdated (GameObjectContainer gameObjectContainer, GameObject gameObject) {
		Notifications.fireEvent(Notifications.obtainEvent(GameObjectLockChanged.class).set(gameObjectContainer, gameObject));
		markContainerChanged(gameObjectContainer);
	}

}
