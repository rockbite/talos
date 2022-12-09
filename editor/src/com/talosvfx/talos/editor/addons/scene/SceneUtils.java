package com.talosvfx.talos.editor.addons.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.esotericsoftware.spine.SkeletonData;
import com.talosvfx.talos.editor.addons.scene.assets.AssetRepository;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.assets.GameAssetType;
import com.talosvfx.talos.editor.addons.scene.events.ComponentAdded;
import com.talosvfx.talos.editor.addons.scene.events.GameObjectCreated;
import com.talosvfx.talos.editor.addons.scene.events.GameObjectDeleted;
import com.talosvfx.talos.editor.addons.scene.events.scene.DeSelectGameObjectExternallyEvent;
import com.talosvfx.talos.editor.addons.scene.events.scene.SelectGameObjectExternallyEvent;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.GameObjectContainer;
import com.talosvfx.talos.editor.addons.scene.logic.Prefab;
import com.talosvfx.talos.editor.addons.scene.logic.components.AComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.MapComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.ParticleComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.ScriptComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.SpineRendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.SpriteRendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import com.talosvfx.talos.editor.addons.scene.maps.TalosLayer;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.serialization.VFXProjectData;
import com.talosvfx.talos.editor.utils.NamingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

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
			component.orderingInLayer = getLatestFreeOrderingIndex(component.sortingLayer);
		}
		component.setGameAsset(spriteAsset);

		return spriteObject;
	}

	public static GameObject createSpineObject (GameObjectContainer gameObjectContainer, GameAsset<SkeletonData> asset, Vector2 sceneCords, GameObject parent) {
		GameObject spineObject = createObjectByTypeName(gameObjectContainer, "spine", sceneCords, parent, asset.nameIdentifier);
		SpineRendererComponent rendererComponent = spineObject.getComponent(SpineRendererComponent.class);

		if (!fromDirectoryView) {
			rendererComponent.orderingInLayer = getLatestFreeOrderingIndex(rendererComponent.sortingLayer);
		}
		rendererComponent.setGameAsset(asset);

		return spineObject;
	}

	public static GameObject createParticle (GameObjectContainer gameObjectContainer, GameAsset<VFXProjectData> asset, Vector2 sceneCords, GameObject parent) {
		GameObject particleObject = createObjectByTypeName(gameObjectContainer, "particle", sceneCords, parent, asset.nameIdentifier);
		ParticleComponent component = particleObject.getComponent(ParticleComponent.class);

		if (!fromDirectoryView) {
			component.orderingInLayer = getLatestFreeOrderingIndex(component.sortingLayer);
		}
		component.setGameAsset(asset);

		return particleObject;
	}

	public static GameObject createFromPrefab (GameObjectContainer gameObjectContainer, GameAsset<Prefab> prefabToCopy, Vector2 position, GameObject parent) {

		Prefab prefab = Prefab.from(prefabToCopy.getRootRawAsset().handle);

		GameObject gameObject = prefab.root.getGameObjects().first();
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
		Notifications.fireEvent(Notifications.obtainEvent(GameObjectCreated.class).setTarget(gameObject));
		SelectGameObjectExternallyEvent selectGameObjectExternallyEvent = Notifications.obtainEvent(SelectGameObjectExternallyEvent.class);
		selectGameObjectExternallyEvent.setGameObject(gameObject);
		Notifications.fireEvent(selectGameObjectExternallyEvent);
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


		onObjectCreated(gameObjectContainer, gameObject);

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

	public static  void repositionGameObject (GameObjectContainer currentContainer, GameObject parentToMoveTo, GameObject childThatHasMoved) {
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
	}


	public static void deleteGameObject (GameObjectContainer gameObjectContainer, GameObject gameObject) {
		DeSelectGameObjectExternallyEvent deSelectGameObjectExternallyEvent = Notifications.obtainEvent(DeSelectGameObjectExternallyEvent.class);
		deSelectGameObjectExternallyEvent.setGameObject(gameObject);
		Notifications.fireEvent(deSelectGameObjectExternallyEvent);

		GameObjectDeleted gameObjectDeleted = Notifications.obtainEvent(GameObjectDeleted.class);
		gameObjectDeleted.setTarget(gameObject);
		Notifications.fireEvent(gameObjectDeleted);
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

	public static void componentAdded (GameObjectContainer currentHolder, GameObject gameObject, ScriptComponent scriptComponent) {
		ComponentAdded componentAdded = Notifications.obtainEvent(ComponentAdded.class);
		componentAdded.setContainer(currentHolder);
		componentAdded.setParent(gameObject);
		componentAdded.setComponent(scriptComponent);
		Notifications.fireEvent(componentAdded);
	}
}
