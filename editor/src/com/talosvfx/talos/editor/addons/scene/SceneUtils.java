package com.talosvfx.talos.editor.addons.scene;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.esotericsoftware.spine.SkeletonData;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.GameAssetType;
import com.talosvfx.talos.editor.addons.scene.events.*;
import com.talosvfx.talos.editor.addons.scene.events.scene.DeSelectGameObjectExternallyEvent;
import com.talosvfx.talos.editor.addons.scene.events.scene.SelectGameObjectExternallyEvent;
import com.talosvfx.talos.editor.addons.scene.logic.*;
import com.talosvfx.talos.editor.addons.scene.logic.components.AComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.MapComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.ParticleComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.SpineRendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.SpriteRendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import com.talosvfx.talos.editor.addons.scene.maps.TalosLayer;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.events.DirectoryChangedEvent;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.project2.TalosProjectData;
import com.talosvfx.talos.editor.project2.apps.ProjectExplorerApp;
import com.talosvfx.talos.editor.serialization.VFXProjectData;
import com.talosvfx.talos.editor.utils.NamingUtils;
import com.talosvfx.talos.editor.utils.Toasts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter.fromDirectoryView;
import static com.talosvfx.talos.editor.addons.scene.widgets.gizmos.SmartTransformGizmo.getLatestFreeOrderingIndex;

public class SceneUtils {

	private static final Logger logger = LoggerFactory.getLogger(SceneUtils.class);

	public static GameObject createEmpty (GameObjectContainer gameObjectContainer, Vector2 position, GameObject parent) {
		return createObjectByTypeName(gameObjectContainer, "empty", position, parent, "empty");
	}

	public static GameObject createSpriteObject (GameObjectContainer gameObjectContainer, GameAsset<Texture> spriteAsset, Vector2 sceneCords, GameObject parent) {
		GameObject spriteObject = createObjectByTypeName(gameObjectContainer, "sprite", sceneCords, parent, spriteAsset.nameIdentifier);
		SpriteRendererComponent component = spriteObject.getComponent(SpriteRendererComponent.class);

		if (!fromDirectoryView) {
			component.orderingInLayer = getLatestFreeOrderingIndex(gameObjectContainer, component.sortingLayer);
		}
		component.setGameAsset(spriteAsset);

		onObjectCreated(gameObjectContainer, spriteObject);

		return spriteObject;
	}

	public static GameObject createSpineObject (GameObjectContainer gameObjectContainer, GameAsset<SkeletonData> asset, Vector2 sceneCords, GameObject parent) {
		GameObject spineObject = createObjectByTypeName(gameObjectContainer, "spine", sceneCords, parent, asset.nameIdentifier);
		SpineRendererComponent rendererComponent = spineObject.getComponent(SpineRendererComponent.class);

		if (!fromDirectoryView) {
			rendererComponent.orderingInLayer = getLatestFreeOrderingIndex(gameObjectContainer, rendererComponent.sortingLayer);
		}
		rendererComponent.setGameAsset(asset);

		onObjectCreated(gameObjectContainer, spineObject);

		return spineObject;
	}

	public static GameObject createParticle (GameObjectContainer gameObjectContainer, GameAsset<VFXProjectData> asset, Vector2 sceneCords, GameObject parent) {
		GameObject particleObject = createObjectByTypeName(gameObjectContainer, "particle", sceneCords, parent, asset.nameIdentifier);
		ParticleComponent component = particleObject.getComponent(ParticleComponent.class);

		if (!fromDirectoryView) {
			component.orderingInLayer = getLatestFreeOrderingIndex(gameObjectContainer, component.sortingLayer);
		}
		component.setGameAsset(asset);

		onObjectCreated(gameObjectContainer, particleObject);

		return particleObject;
	}

	public static GameObject createFromPrefab (GameObjectContainer gameObjectContainer, GameAsset<Prefab> prefabToCopy, Vector2 position, GameObject parent) {

		Prefab prefab = new Prefab(prefabToCopy.getRootRawAsset().handle);

		GameObject gameObject = prefab.root;
		TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);
		transformComponent.position.set(position);

		String name = NamingUtils.getNewName(prefab.name, gameObjectContainer.getAllGONames());

		gameObject.setName(name);

		randomizeChildrenUUID(gameObject);
		if (parent == null) {
			gameObjectContainer.addGameObject(gameObject);
		} else {
			parent.addGameObject(gameObject);
		}


		onObjectCreated(gameObjectContainer, gameObject);

		return gameObject;
	}

	private static void onObjectCreated (GameObjectContainer gameObjectContainer, GameObject gameObject) {
		Notifications.fireEvent(Notifications.obtainEvent(GameObjectCreated.class).set(gameObjectContainer, gameObject));
		SelectGameObjectExternallyEvent selectGameObjectExternallyEvent = Notifications.obtainEvent(SelectGameObjectExternallyEvent.class);
		selectGameObjectExternallyEvent.setGameObject(gameObject);
		Notifications.fireEvent(selectGameObjectExternallyEvent);

		markContainerChanged(gameObjectContainer);

	}

	public static GameObject createObjectByTypeName (GameObjectContainer gameObjectContainer, String idName, Vector2 position, GameObject parent, String nameHint) {
		GameObject gameObject = new GameObject();

		XmlReader.Element template = SharedResources.configData.getGameObjectConfigurationMap().get(idName);

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

		return gameObject;
	}

	private static void initComponentsFromTemplate (GameObject gameObject, XmlReader.Element template) {
		XmlReader.Element container = template.getChildByName("components");
		Array<XmlReader.Element> componentsXMLArray = container.getChildrenByName("component");
		for (XmlReader.Element componentXML : componentsXMLArray) {
			String className = componentXML.getAttribute("className");
			String classPath = SharedResources.configData.getComponentClassPath();

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


		SelectGameObjectExternallyEvent selectGameObjectExternallyEvent = Notifications.obtainEvent(SelectGameObjectExternallyEvent.class);
		selectGameObjectExternallyEvent.setGameObject(childThatHasMoved);
		Notifications.fireEvent(selectGameObjectExternallyEvent);

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

	private static ObjectMap<GameObjectContainer, Array<GameObject>> copyPasteBuffer = new ObjectMap<>();

	public static void copy (GameObjectContainer currentContainer, Array<GameObject> arraySelection) {
		copyPasteBuffer.put(currentContainer, arraySelection);
	}

	public static void paste (GameObjectContainer currentContainer) {
		logger.info("Needs a rethink");
		if (copyPasteBuffer.containsKey(currentContainer)) {
			if (copyPasteBuffer.get(currentContainer).isEmpty()) {

			}
		}
	}

	public static void componentAdded (GameObjectContainer currentHolder, GameObject gameObject, AComponent component) {
		ComponentAdded componentAdded = Notifications.obtainEvent(ComponentAdded.class);
		componentAdded.setContainer(currentHolder);
		componentAdded.setParent(gameObject);
		componentAdded.setComponent(component);
		Notifications.fireEvent(componentAdded);

		markContainerChanged(currentHolder);
	}

	public static void componentUpdated (GameObjectContainer gameObjectContainer, GameObject gameObject, AComponent component) {
		componentUpdated(gameObjectContainer, gameObject, component, false);
	}

	public static void componentUpdated (GameObjectContainer gameObjectContainer, GameObject gameObject, AComponent component, boolean isRapid) {
		ComponentUpdated componentUpdated = Notifications.obtainEvent(ComponentUpdated.class);
		componentUpdated.setComponent(component);
		componentUpdated.setParent(gameObject);
		componentUpdated.setRapid(isRapid);
		componentUpdated.setContainer(gameObjectContainer);
		Notifications.fireEvent(componentUpdated);

		if (!isRapid) {
			markContainerChanged(gameObjectContainer);
		}
	}

	public static void layersUpdated () {
		TalosProjectData currentProject = SharedResources.currentProject;
		currentProject.save();
		Notifications.fireEvent(Notifications.obtainEvent(LayerListUpdated.class));
	}

	private static void markContainerChanged (GameObjectContainer currentHolder) {
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

		GameObject gamePrefab = new GameObject();
		gamePrefab.setName("Prefab");
		gamePrefab.addGameObject(gameObject);

		Prefab prefab = new Prefab(gamePrefab);

		prefabHandle.writeString(prefab.getAsString(), false);
		AssetRepository.getInstance().rawAssetCreated(prefabHandle, true);

		Notifications.fireEvent(Notifications.obtainEvent(DirectoryChangedEvent.class).set(prefabs.path()));
	}

}
