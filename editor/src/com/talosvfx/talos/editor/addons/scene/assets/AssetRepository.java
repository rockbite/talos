
package com.talosvfx.talos.editor.addons.scene.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.esotericsoftware.spine.SkeletonBinary;
import com.esotericsoftware.spine.SkeletonData;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.addons.scene.events.AssetPathChanged;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.Prefab;
import com.talosvfx.talos.editor.addons.scene.logic.TilePaletteData;
import com.talosvfx.talos.editor.addons.scene.logic.components.AComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.GameResourceOwner;
import com.talosvfx.talos.editor.addons.scene.logic.components.MapComponent;
import com.talosvfx.talos.editor.addons.scene.utils.AMetadata;
import com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.DirectoryMetadata;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.SpineMetadata;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.runtime.ParticleEffectDescriptor;
import com.talosvfx.talos.runtime.assets.AssetProvider;
import com.talosvfx.talos.runtime.serialization.ExportData;

import java.util.Objects;
import java.util.UUID;

import static com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter.relative;
import static com.talosvfx.talos.editor.project.TalosProject.exportTLSDataToP;
import static com.talosvfx.talos.editor.serialization.ProjectSerializer.writeTalosPExport;

public class AssetRepository {

	private ObjectMap<GameAssetType, ObjectMap<String, GameAsset<?>>> identifierGameAssetMap = new ObjectMap<>();

	public <T> GameAsset<T> getAssetForIdentifier (String identifier, GameAssetType type) {
		if (identifierGameAssetMap.containsKey(type)) {
			if (identifierGameAssetMap.get(type).containsKey(identifier)) {
				return (GameAsset<T>)identifierGameAssetMap.get(type).get(identifier);
			}
		}
		GameAsset<T> brokenAsset = new GameAsset<>(identifier, type);
		brokenAsset.setBroken(new Exception("No asset found"));
		return brokenAsset;
	}

	private <T> void putAssetForIdentifier (String identifier, GameAssetType type, GameAsset<T> asset) {
		if (!identifierGameAssetMap.containsKey(type)) {
			identifierGameAssetMap.put(type, new ObjectMap<>());
		}
		identifierGameAssetMap.get(type).put(identifier, asset);
	}


	private ObjectMap<FileHandle, GameAsset> fileHandleGameAssetObjectMap = new ObjectMap<>();
	private ObjectMap<UUID, RawAsset> uuidRawAssetMap = new ObjectMap<>();
	private ObjectMap<FileHandle, RawAsset> fileHandleRawAssetMap = new ObjectMap<>();

	private FileHandle assetsRoot;
	private Json json;

	public TextureRegion brokenTextureRegion;

	static AssetRepository instance;
	public static AssetRepository getInstance() {
		if(instance == null) init();
		return instance;
	}
	public static void init () {
		AssetRepository assetRepository = new AssetRepository();
		AssetRepository.instance = assetRepository;
	}

	public AssetRepository () {
		json = new Json();
		json.setOutputType(JsonWriter.OutputType.json);

		brokenTextureRegion = new TextureRegion(new Texture(Gdx.files.internal("addons/scene/missing/missing.png")));
	}

	public void loadAssetsForProject (FileHandle assetsRoot) {
		this.assetsRoot = assetsRoot;


		//Go over all files, create raw assets if they don't exist in the map
		if (this.assetsRoot.isDirectory()) {
			collectRawResourceFromDirectory(this.assetsRoot, false);
		}

		//Go over all raw assets, and create game resources
		//Game resources need to be able to search for the raw assets to link

		checkAllGameAssetCreation();
	}

	private void checkAllGameAssetCreation () { //raws
		checkGameAssetCreation(GameAssetType.SPRITE);
		checkGameAssetCreation(GameAssetType.SCRIPT);
		checkGameAssetCreation(GameAssetType.TWEEN);
		checkGameAssetCreation(GameAssetType.ATLAS);
		checkGameAssetCreation(GameAssetType.SOUND);

		checkGameAssetCreation(GameAssetType.SKELETON);

		checkGameAssetCreation(GameAssetType.VFX);
		checkGameAssetCreation(GameAssetType.PREFAB);

		checkGameAssetCreation(GameAssetType.TILE_PALETTE);
	}

	private void checkGameAssetCreation (GameAssetType type) {
		//We need to do multiple passes here for dependent assets

		for (ObjectMap.Entry<FileHandle, RawAsset> entry : fileHandleRawAssetMap) {
			FileHandle key = entry.key;
			RawAsset value = entry.value;


			if (key.isDirectory()) continue;

			GameAssetType assetTypeFromExtension = GameAssetType.getAssetTypeFromExtension(key.extension());
			if (type != assetTypeFromExtension) continue;

			if (assetTypeFromExtension.isRootGameAsset()) {
				createGameAsset(key, value);
			}
		}
	}

	public void reloadGameAssetForRawFile (RawAsset link) {
		Array<GameAsset> gameAssetReferences = new Array<>();
		gameAssetReferences.addAll(link.gameAssetReferences);

		for (GameAsset gameAssetReference : gameAssetReferences) {
			reloadGameAsset(gameAssetReference);
		}
	}

	private void reloadGameAsset (GameAsset gameAssetReference) {
		RawAsset rootRawAsset = gameAssetReference.getRootRawAsset();
		String gameAssetIdentifier = getGameAssetIdentifierFromRawAsset(rootRawAsset);

		GameAssetType assetTypeFromExtension = GameAssetType.getAssetTypeFromExtension(rootRawAsset.handle.extension());

		try {
			GameAsset gameAsset = createGameAssetForType(assetTypeFromExtension, gameAssetIdentifier, rootRawAsset, false);
			gameAssetReference.setResourcePayload(gameAsset.getResource());
			gameAssetReference.setUpdated();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	static class TypeIdentifierPair {
		GameAssetType type;
		String identifier;

		public TypeIdentifierPair (GameAssetType type, String gameResourceIdentifier) {
			this.type = type;
			this.identifier = gameResourceIdentifier;
		}

		@Override
		public boolean equals (Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			TypeIdentifierPair that = (TypeIdentifierPair)o;
			return type == that.type && identifier.equals(that.identifier);
		}

		@Override
		public int hashCode () {
			return Objects.hash(type, identifier);
		}
	}

	private void collectExportedAssetsArrayGameObjects (JsonValue gameObjects, ObjectSet<TypeIdentifierPair> pairs) {
		if (gameObjects != null) {
			for (JsonValue gameObject : gameObjects) {
				//Grab each component
				collectGameObjectExportedAssets(pairs, gameObject);
			}
		}
	}

	private void collectGameObjectExportedAssets (ObjectSet<TypeIdentifierPair> pairs, JsonValue gameObject) {
		if (gameObject.has("components")) {
			JsonValue components = gameObject.get("components");
			for (JsonValue component : components) {
				String componentClazz = component.getString("class");
				if (componentIsResourceOwner(componentClazz)) {
					//Lets grab the game resource

					GameAssetType type = getGameAssetTypeFromClazz(componentClazz);

					String gameResourceIdentifier = GameResourceOwner.readGameResourceFromComponent(component);
					pairs.add(new TypeIdentifierPair(type, gameResourceIdentifier));
				}
				if (componentClazz.equals(MapComponent.class.getName())) {
					JsonValue layers = component.get("layers");

					for (int i = 0; i < layers.size; i++) {
						JsonValue layer = layers.get(i);
						String gameResourceIdentifier = GameResourceOwner.readGameResourceFromComponent(layer);
						pairs.add(new TypeIdentifierPair(GameAssetType.TILE_PALETTE, gameResourceIdentifier));
					}

				}
			}
		}
		if (gameObject.has("children")) {
			JsonValue children = gameObject.get("children");
			collectExportedAssetsArrayGameObjects(children, pairs);
		}
	}

	//Export formats
	public void exportToFile () { //todo
		//Go over all entities, go over all components. If component has a game resource, we mark it for export

		FileHandle scenes = Gdx.files.absolute(SceneEditorWorkspace.getInstance().getProjectPath()).child("scenes");
		ObjectSet<TypeIdentifierPair> identifiersBeingUsedByComponents = new ObjectSet<>();
		if (scenes.exists()) {
			for (FileHandle handle : scenes.list()) {
				JsonValue scene = new JsonReader().parse(handle);

				JsonValue gameObjects = scene.get("gameObjects");
				collectExportedAssetsArrayGameObjects(gameObjects, identifiersBeingUsedByComponents);
			}
		}

		//Find all prefabs
		FileHandle assets = Gdx.files.absolute(SceneEditorWorkspace.getInstance().getProjectPath()).child("assets");
		//Find all prefabs and do same shit as above, export the prefab GameAsset as well as the .prefabs
		findAllPrefabs(assets, identifiersBeingUsedByComponents);


		Array<GameAsset<?>> gameAssetsToExport = new Array<>();
		for (TypeIdentifierPair identPair : identifiersBeingUsedByComponents) {

			//we need to get the identifier and type pairs

			GameAsset<?> gameAsset = getAssetForIdentifier(identPair.identifier, identPair.type);
			if (gameAsset == null) {
				System.out.println("Game asset is null, not exporting");
				continue;
			}
			if (gameAsset.isBroken()) {
				System.out.println("Game asset is broken, not exporting");
				continue;
			}

			gameAssetsToExport.add(gameAsset);
			gameAssetsToExport.addAll(gameAsset.dependentGameAssets); //Add any dependnet game assets
		}

		GameAssetsExportStructure gameAssetExportStructure = new GameAssetsExportStructure();

		for (GameAsset<?> gameAsset : gameAssetsToExport) {
			GameAssetExportStructure assetExportStructure = new GameAssetExportStructure();
			assetExportStructure.identifier = gameAsset.nameIdentifier;
			assetExportStructure.type = gameAsset.type;
			for (RawAsset dependentRawAsset : gameAsset.dependentRawAssets) {
				assetExportStructure.absolutePathsOfRawFiles.add(dependentRawAsset.handle.path());
			}
			gameAssetExportStructure.gameAssets.add(assetExportStructure);
		}

		FileHandle assetRepoExportFile = Gdx.files.absolute(SceneEditorWorkspace.getInstance().getProjectPath()).child("assetExport.json");
		assetRepoExportFile.writeString(json.toJson(gameAssetExportStructure), false);


	}

	private void findAllPrefabs (FileHandle assets, ObjectSet<TypeIdentifierPair> identifiersBeingUsedByComponents) {
		FileHandle[] list = assets.list();
		for (FileHandle handle : list) {
			if (handle.isDirectory()) {
				findAllPrefabs(handle, identifiersBeingUsedByComponents);
			} else {
				if (handle.extension().equals("prefab")) {
					//Get the raw asset
					GameAsset<Prefab> gameAsset = fileHandleGameAssetObjectMap.get(handle);
					if (gameAsset != null) {
						identifiersBeingUsedByComponents.add(new TypeIdentifierPair(GameAssetType.PREFAB, gameAsset.nameIdentifier));

						JsonValue prefab = new JsonReader().parse(gameAsset.getRootRawAsset().handle);
						JsonValue gameObject = prefab.get("root");

						//Get all dependent assets of this prefab
						collectGameObjectExportedAssets(identifiersBeingUsedByComponents, gameObject);
					}
				}
			}
		}
	}

	private GameAssetType getGameAssetTypeFromClazz (String componentClazz) {
		try {
			Class<? extends GameResourceOwner> aClass = ClassReflection.forName(componentClazz);
			return GameAssetType.getAssetTypeFromGameResourceOwner(aClass);
		} catch (ReflectionException e) {
			throw new RuntimeException(e);
		}
	}

	public static class GameAssetExportStructure {
		String identifier;
		GameAssetType type;
		Array<String> absolutePathsOfRawFiles = new Array<>();
	}

	public static class GameAssetsExportStructure {
		Array<GameAssetExportStructure> gameAssets = new Array<>();
	}

	private boolean componentIsResourceOwner (String componentClazz) {
		try {
			Class aClass = ClassReflection.forName(componentClazz);

			return GameResourceOwner.class.isAssignableFrom(aClass);
		} catch (ReflectionException e) {
			e.printStackTrace();
			return false;
		}
	}

	private String getGameAssetIdentifierFromRawAsset (RawAsset asset) {
		return asset.handle.nameWithoutExtension();
	}

	private void createGameAsset (FileHandle key, RawAsset value) {
		String gameAssetIdentifier = getGameAssetIdentifierFromRawAsset(value);



		GameAssetType assetTypeFromExtension = GameAssetType.getAssetTypeFromExtension(key.extension());

		boolean shouldUpdate = false;
		if (getAssetForIdentifier(gameAssetIdentifier, assetTypeFromExtension) != null) {
			shouldUpdate = true;
		}

		GameAsset gameAsset = createGameAssetForType(assetTypeFromExtension, gameAssetIdentifier, value, true);


		if (shouldUpdate) {
			GameAsset<?> oldGameAsset = getAssetForIdentifier(gameAssetIdentifier, assetTypeFromExtension);
			for (RawAsset dependentRawAsset : oldGameAsset.dependentRawAssets) {
				dependentRawAsset.gameAssetReferences.removeValue(oldGameAsset, true);
			}
			Array<GameAsset.GameAssetUpdateListener> listeners = oldGameAsset.listeners;
			gameAsset.listeners.addAll(listeners);
			oldGameAsset.listeners.clear();

			//Find all components in the current scene that was using this and reset their game asset to the new one
			Array<GameObject> list = new Array<>();
			SceneEditorWorkspace sceneEditorWorkspace = SceneEditorWorkspace.getInstance();
			if (sceneEditorWorkspace != null && sceneEditorWorkspace.getRootGO() != null) {
				sceneEditorWorkspace.getRootGO().getChildrenByComponent(GameResourceOwner.class, list);
				for (GameObject gameObject : list) {
					for (AComponent component : gameObject.getComponents()) {
						if (component instanceof GameResourceOwner) {
							GameResourceOwner<?> gameResourceOwner = (GameResourceOwner<?>)component;

							GameAsset<?> gameResource = gameResourceOwner.getGameResource();
							if (gameResource.type == GameAssetType.SKELETON) {
								System.out.println();
							}

							if (gameResourceOwner.getGameResource().equals(oldGameAsset)) {
								gameResourceOwner.setGameAsset(gameAsset);
							}
						}
					}
				}
			}
		}

		if (shouldUpdate) {
			gameAsset.setUpdated();
		}


		if (gameAsset == null) return;
		System.out.println("Registering game asset " + gameAssetIdentifier + " " + gameAsset + " " + value.handle.path() + " " + assetTypeFromExtension);

		putAssetForIdentifier(gameAssetIdentifier, assetTypeFromExtension, gameAsset);
		fileHandleGameAssetObjectMap.put(key, gameAsset);
	}

	public GameAsset<?> createGameAssetForType (GameAssetType assetTypeFromExtension, String gameAssetIdentifier, RawAsset value, boolean createLinks) {
		if (!assetTypeFromExtension.isRootGameAsset()) {
			throw new GdxRuntimeException("Trying to load a game asset from a non root asset");
		}

		GameAsset<?> gameAssetOut = null;


		try {
			switch (assetTypeFromExtension) {
			case SPRITE:
				GameAsset<Texture> textureGameAsset = new GameAsset<>(gameAssetIdentifier, assetTypeFromExtension);
				gameAssetOut = textureGameAsset;

				textureGameAsset.setResourcePayload(new Texture(value.handle));

				if (createLinks) {
					value.gameAssetReferences.add(textureGameAsset);
					textureGameAsset.dependentRawAssets.add(value);
				}

				break;
			case ATLAS:
				GameAsset<TextureAtlas> textureAtlasGameAsset = new GameAsset<>(gameAssetIdentifier, assetTypeFromExtension);
				gameAssetOut = textureAtlasGameAsset;

				TextureAtlas.TextureAtlasData textureAtlasData = new TextureAtlas.TextureAtlasData(value.handle, value.handle.parent(), false);
				TextureAtlas atlas = new TextureAtlas(textureAtlasData);
				textureAtlasGameAsset.setResourcePayload(atlas);

				if (createLinks) {
					value.gameAssetReferences.add(textureAtlasGameAsset);
					textureAtlasGameAsset.dependentRawAssets.add(value);

					for (TextureAtlas.TextureAtlasData.Page page : textureAtlasData.getPages()) {
						FileHandle textureFile = page.textureFile;
						if (!fileHandleRawAssetMap.containsKey(textureFile)) {
							throw new GdxRuntimeException("Corruption, texture file does not exist" + textureFile);
						}

						RawAsset rawAssetForPage = fileHandleRawAssetMap.get(textureFile);

						rawAssetForPage.gameAssetReferences.add(textureAtlasGameAsset);
						textureAtlasGameAsset.dependentRawAssets.add(rawAssetForPage);
					}
				}



				break;

			case SKELETON:

				GameAsset<SkeletonData> skeletonDataGameAsset = new GameAsset<>(gameAssetIdentifier, assetTypeFromExtension);
				gameAssetOut = skeletonDataGameAsset;

				//Gotta try load the atlas
				String skeleName = value.handle.nameWithoutExtension();
				FileHandle atlasFile = value.handle.parent().child(skeleName + ".atlas");
				TextureAtlas.TextureAtlasData skeleAtlasData = new TextureAtlas.TextureAtlasData(atlasFile, atlasFile.parent(), false);
				TextureAtlas skeleAtlas = new TextureAtlas(skeleAtlasData);

				if (atlasFile.exists()) {
					SkeletonBinary skeletonBinary = new SkeletonBinary(skeleAtlas);

					SpineMetadata metaData = (SpineMetadata)value.metaData;

					skeletonBinary.setScale(1f/metaData.pixelsPerUnit);

					SkeletonData skeletonData = skeletonBinary.readSkeletonData(value.handle);
					skeletonDataGameAsset.setResourcePayload(skeletonData);

					if (createLinks) {
						value.gameAssetReferences.add(skeletonDataGameAsset);
						skeletonDataGameAsset.dependentRawAssets.add(value);

						RawAsset skeleAtlasRawAsset = fileHandleRawAssetMap.get(atlasFile);
						skeletonDataGameAsset.dependentRawAssets.add(skeleAtlasRawAsset);

						for (TextureAtlas.TextureAtlasData.Page page : skeleAtlasData.getPages()) {
							FileHandle textureFile = page.textureFile;
							if (!fileHandleRawAssetMap.containsKey(textureFile)) {
								throw new GdxRuntimeException("Corruption, texture file does not exist" + textureFile);
							}

							RawAsset rawAssetForPage = fileHandleRawAssetMap.get(textureFile);
							rawAssetForPage.gameAssetReferences.add(skeletonDataGameAsset);
							skeletonDataGameAsset.dependentRawAssets.add(rawAssetForPage);
						}
					}

				}

				break;
			case SOUND:
				break;
			case VFX:

				GameAsset<ParticleEffectDescriptor> particleEffectDescriptorGameAsset = new GameAsset<>(gameAssetIdentifier, assetTypeFromExtension);

				gameAssetOut = particleEffectDescriptorGameAsset;

				if (createLinks) {
					particleEffectDescriptorGameAsset.dependentRawAssets.add(value);
				}

				ParticleEffectDescriptor particleEffectDescriptor = new ParticleEffectDescriptor();
				particleEffectDescriptor.setAssetProvider(new AssetProvider() {
					@Override
					public <T> T findAsset (String assetName, Class<T> clazz) {

						if (Sprite.class.isAssignableFrom(clazz)) {
							GameAsset<Texture> gameAsset = getAssetForIdentifier(assetName, GameAssetType.SPRITE);

							if (gameAsset != null) {
								if (createLinks) {
									for (RawAsset dependentRawAsset : gameAsset.dependentRawAssets) {
										particleEffectDescriptorGameAsset.dependentRawAssets.add(dependentRawAsset);
									}
								}
								return (T)new Sprite(gameAsset.getResource());
							} else {
								particleEffectDescriptorGameAsset.setBroken(new Exception("Cannot find " + assetName));
							}
						}

						throw new GdxRuntimeException("Couldn't find asset " + assetName + " for type " + clazz);
					}
				});

				//We need to find the asset that is linked to
				//P should be same directory, lets find it

				FileHandle pFile = value.handle.parent().child(value.handle.nameWithoutExtension() + ".p");
				if (!pFile.exists()) {
					throw new GdxRuntimeException("No p file for tls " + value.handle.path() + " " + pFile.path());
				}

				RawAsset rawAssetPFile = fileHandleRawAssetMap.get(pFile);

				try {
					particleEffectDescriptor.load(rawAssetPFile.handle);
				} catch (Exception e) {
					System.out.println("Failure to load particle effect");
					throw e;
				}

				particleEffectDescriptorGameAsset.setResourcePayload(particleEffectDescriptor);

				if (createLinks) {
					value.gameAssetReferences.add(particleEffectDescriptorGameAsset);
					particleEffectDescriptorGameAsset.dependentRawAssets.add(rawAssetPFile);
				}

				break;
			case VFX_OUTPUT:
				break;
			case SCRIPT:

				GameAsset<String> scriptGameAsset = new GameAsset<>(gameAssetIdentifier, assetTypeFromExtension);
				gameAssetOut = scriptGameAsset;

				scriptGameAsset.setResourcePayload("ScriptDummy");

				if (createLinks) {
					value.gameAssetReferences.add(scriptGameAsset);
					scriptGameAsset.dependentRawAssets.add(value);
				}

				break;
			case TWEEN:
				GameAsset<String> tweenGameAsset = new GameAsset<>(gameAssetIdentifier, assetTypeFromExtension);
				gameAssetOut = tweenGameAsset;
				tweenGameAsset.setResourcePayload("Dummy");

				if (createLinks) {
					value.gameAssetReferences.add(tweenGameAsset);
					tweenGameAsset.dependentRawAssets.add(value);
				}

				break;
			case PREFAB:
				GameAsset<Prefab> prefabGameAsset = new GameAsset<>(gameAssetIdentifier, assetTypeFromExtension);
				gameAssetOut = prefabGameAsset;

				Prefab prefab = Prefab.from(value.handle);

				prefabGameAsset.setResourcePayload(prefab);

				if (createLinks) {
					value.gameAssetReferences.add(prefabGameAsset);
					prefabGameAsset.dependentRawAssets.add(value);
				}

				break;
			case TILE_PALETTE:
				GameAsset<TilePaletteData> paletteGameAsset = new GameAsset<>(gameAssetIdentifier, assetTypeFromExtension);
				gameAssetOut = paletteGameAsset;

				TilePaletteData paletteData = json.fromJson(TilePaletteData.class, value.handle);

				paletteGameAsset.setResourcePayload(paletteData);

				if (createLinks) {
					value.gameAssetReferences.add(paletteGameAsset);
					paletteGameAsset.dependentRawAssets.add(value);

					for (ObjectMap.Entry<UUID, GameAsset<?>> reference : paletteData.references) {
						//Add the dependent game asset's root assets
						paletteGameAsset.dependentGameAssets.addAll(reference.value);
					}
				}

				break;
			case DIRECTORY:
				break;
			}
		} catch (Exception e) {
			if (gameAssetOut != null) {
				e.printStackTrace();
				gameAssetOut.setBroken(e);
				System.out.println("Marking asset as broken " + gameAssetOut + " " + value.handle.path());
			}
		}

		return gameAssetOut;
	}

	public void saveGameAssetResourceJsonToFile (GameAsset<?> gameAsset) {
		RawAsset rootRawAsset = gameAsset.getRootRawAsset();

		String jsonString = json.toJson(gameAsset.getResource());
		rootRawAsset.handle.writeString(jsonString, false);
	}

	public GameObject copyGameObject (GameObject gameObject) {
		String serialized = json.toJson(gameObject);
		GameObject newObject = json.fromJson(GameObject.class, serialized);
		newObject.uuid = UUID.randomUUID();
		return newObject;
	}

	private void collectRawResourceFromDirectory (FileHandle dir, boolean checkGameResources) {

		if (!dir.isDirectory()) {
			rawAssetCreated(dir, checkGameResources);
		}

		FileHandle[] list = dir.list();
		for (FileHandle fileHandle : list) {

			if (fileHandle.isDirectory()) {
				collectRawResourceFromDirectory(fileHandle, checkGameResources);
			} else if (shouldIgnoreAsset(fileHandle)) {
				continue;
			} else if (!fileHandleRawAssetMap.containsKey(fileHandle)) {
				rawAssetCreated(fileHandle, checkGameResources);
			}
		}
	}

	private boolean shouldIgnoreAsset (FileHandle fileHandle) {
		String extension = fileHandle.extension();

		if (fileHandle.name().equals(".DS_Store")) return true;
		if (extension.equals("meta")) return true;

		return false;
	}

	public void registerFileWatching () {

	}

	public void updateVFXTLS (RawAsset tlsRawAsset, boolean checkGameResources) {
		ExportData exportData = exportTLSDataToP(tlsRawAsset.handle);
		String exportDataJson = writeTalosPExport(exportData);

		FileHandle exportedPFile = tlsRawAsset.handle.parent().child(tlsRawAsset.handle.nameWithoutExtension() + ".p");
		exportedPFile.writeString(exportDataJson, false);

		rawAssetCreated(exportedPFile, checkGameResources);
	}

	public void rawAssetCreated (FileHandle fileHandle, boolean checkGameResources) {
		GameAssetType assetTypeFromExtension = GameAssetType.getAssetTypeFromExtension(fileHandle.extension());

		RawAsset rawAsset = new RawAsset(fileHandle);

		if (assetTypeFromExtension == GameAssetType.VFX) {
			updateVFXTLS(rawAsset, checkGameResources);
		}

		FileHandle metadataHandleFor = AssetImporter.getMetadataHandleFor(fileHandle);
		if (metadataHandleFor.exists()) {
			try {
				Class<? extends AMetadata> metaClassForType = GameAssetType.getMetaClassForType(assetTypeFromExtension);
				rawAsset.metaData = json.fromJson(metaClassForType, metadataHandleFor);
				rawAsset.metaData.setLinkRawAsset(rawAsset);
			} catch (Exception e) {
				e.printStackTrace();

				System.out.println("Error reading meta for " + metadataHandleFor.path() + " " + metadataHandleFor.readString());
				rawAsset.metaData = createMetaDataForAsset(rawAsset);
			}
		} else {
			rawAsset.metaData = createMetaDataForAsset(rawAsset);
		}


		uuidRawAssetMap.put(rawAsset.metaData.uuid, rawAsset);
		fileHandleRawAssetMap.put(fileHandle, rawAsset);

		System.out.println("Raw asset created" + rawAsset.handle.path());

		if (checkGameResources) {
			checkAllGameAssetCreation();
		}
	}

	private <T extends AMetadata> T createMetaDataForAsset (RawAsset rawAsset) {
		if (rawAsset.handle.isDirectory()) {
			return (T)new DirectoryMetadata();
		} else {

			String extension = rawAsset.handle.extension();

			GameAssetType assetTypeFromExtension = GameAssetType.getAssetTypeFromExtension(extension);
			T metaForType = (T)GameAssetType.createMetaForType(assetTypeFromExtension);

			//Save the meta data
			FileHandle metadataHandleFor = AssetImporter.getMetadataHandleFor(rawAsset.handle);
			metadataHandleFor.writeString(json.prettyPrint(metaForType), false);

			metaForType.setLinkRawAsset(rawAsset);

			return metaForType;
		}
	}

	public GameAsset<?> getAssetForPath (FileHandle file, boolean ignoreBroken) {
		if (fileHandleGameAssetObjectMap.containsKey(file)) {
			GameAsset<?> gameAsset = fileHandleGameAssetObjectMap.get(file);
			if (ignoreBroken) {
				return gameAsset;
			} else {
				if (!gameAsset.isBroken()) {
					return gameAsset;
				} else {
					return null;
				}
			}
		}
		return null;
	}

	private void deleteFileImpl (FileHandle handle) {
		FileHandle metadataHandleFor = AssetImporter.getMetadataHandleFor(handle);
		if (metadataHandleFor.exists()) {
			metadataHandleFor.delete();
		}
		handle.delete();
		Array<GameAsset> gameAssetsToUpdate = new Array<>();

		if (fileHandleRawAssetMap.containsKey(handle)) {
			RawAsset rawAsset = fileHandleRawAssetMap.get(handle);
			gameAssetsToUpdate.addAll(rawAsset.gameAssetReferences);


			for (GameAsset gameAsset : gameAssetsToUpdate) {
				gameAsset.dependentRawAssets.removeValue(rawAsset, true);

				//check if this is a broken game asset, or one to be removed
				if (gameAsset.dependentRawAssets.size > 0) {
					gameAsset.setBroken(new Exception("Removed one of the dependent raw files"));
					gameAsset.setUpdated();
				} else {
					gameAsset.setBroken(new Exception("Game Asset Removed"));
					gameAsset.setUpdated();
					fileHandleGameAssetObjectMap.remove(rawAsset.handle);
					identifierGameAssetMap.get(gameAsset.type).remove(gameAsset.nameIdentifier);
				}

			}

			fileHandleRawAssetMap.remove(handle);
			uuidRawAssetMap.remove(rawAsset.metaData.uuid);
		}
	}
	public void deleteRawAsset (FileHandle handle) {
		if (handle.isDirectory()) {
			for (FileHandle fileHandle : handle.list()) {
				deleteRawAsset(fileHandle);
			}
		}
		deleteFileImpl(handle);

	}

	public void copyRawAsset (FileHandle file, FileHandle directory) {

		if (directory.child(file.name()).exists()) {

			//The file needs to be renamed before copying
			String name = suggestNewName(file, directory);

			if (file.isDirectory()) {
				FileHandle[] list = file.list();

				//Change the destination and copy all its children into the new destination
				directory = directory.child(name);
				directory.mkdirs();

				for (FileHandle fileHandle : list) {
					if (fileHandle.extension().equals("meta")) continue; //Don't copy meta

					fileHandle.copyTo(directory);
				}

			} else {
				directory = directory.child(name);

				//Its a file, so we just change the name and use copyTo
				file.copyTo(directory);
			}
		} else {
			//Its just a copy to destination

			if (file.isDirectory()) {
				FileHandle[] list = file.list();//Get the list before making the directory to make sure its excluded
				FileHandle dest = directory.child(file.name());
				dest.mkdirs();
				for (FileHandle fileHandle : list) {
					if (fileHandle.extension().equals("meta")) continue; //Don't copy meta
					fileHandle.copyTo(dest);
				}
			} else {
				file.copyTo(directory);
			}
		}

		collectRawResourceFromDirectory(directory, true);
	}

	private static String suggestNewName (FileHandle file, FileHandle directory) {
		int i = 0;
		String nameWithoutExtension = file.nameWithoutExtension();
		if (nameWithoutExtension.contains("_Copy_")) {
			nameWithoutExtension = nameWithoutExtension.split("_Copy_")[0];
		}

		while (i < 1000) {
			String testName = nameWithoutExtension + "_Copy_" + i;
			if (!file.isDirectory()) {
				testName += "." + file.extension();
			}
			if (directory.child(testName).exists()) {
				i++;
			} else {
				return testName;
			}
		}
		return "Too_Many_File_Attempts";
	}

	static class MovingDirNode {
		FileHandle oldHandle;
		FileHandle newHandle;

		Array<MovingDirNode> children = new Array<>();
	}


	private void updateChildReferences (MovingDirNode parent) {
		for (MovingDirNode child : parent.children) {

			FileHandle oldHandle = child.oldHandle;

			FileHandle newHandle = parent.newHandle.child(oldHandle.name());
			child.newHandle = newHandle;


			if (!newHandle.isDirectory()) {
				RawAsset rawAsset = fileHandleRawAssetMap.remove(oldHandle);

				if (rawAsset == null) {
					System.out.println();
				}
				rawAsset.handle = newHandle;

				fileHandleRawAssetMap.put(newHandle, rawAsset);

				AssetPathChanged assetPathChanged = Notifications.obtainEvent(AssetPathChanged.class);
				assetPathChanged.oldRelativePath = relative(oldHandle);
				assetPathChanged.newRelativePath = relative(newHandle);
				Notifications.fireEvent(assetPathChanged);

				for (GameAsset gameAssetReference : rawAsset.gameAssetReferences) {
					gameAssetReference.setUpdated();
				}

				if (isRootGameResource(rawAsset)) {
					GameAsset gameAsset = fileHandleGameAssetObjectMap.remove(oldHandle);
					fileHandleGameAssetObjectMap.put(newHandle, gameAsset);

					gameAsset.setUpdated();
				}
			}
			updateChildReferences(child);

		}
	}
	private void populateChildren (FileHandle fileHandle, MovingDirNode fileNode) {
		FileHandle[] children = fileHandle.list();
		for (FileHandle handle : children) {
			if (handle.extension().equals("meta")) continue;

			MovingDirNode value = new MovingDirNode();

			value.oldHandle = handle;
			fileNode.children.add(value);

			if (handle.isDirectory()) {
				populateChildren(handle, value);
			}
		}
	}

	//Could be a rename or a move
	public void moveFile (FileHandle file, FileHandle destination) {
		AssetImporter.moveFile(file, destination, true);
	}
	public void moveFile (FileHandle file, FileHandle destination, boolean checkGameAssets) {


		if (file.isDirectory()) {
			//Moving a folder
			if (destination.exists() && !destination.isDirectory()) {
				throw new GdxRuntimeException("Trying to move a directory to a file");
			}

			MovingDirNode rootNode = new MovingDirNode();
			rootNode.oldHandle = file;
			rootNode.newHandle = destination;
			populateChildren(file, rootNode);

			file.moveTo(destination);

			updateChildReferences(rootNode);

		} else {
			//Moving a file
			if (destination.isDirectory()) {
				//Moving a file into a directory

				RawAsset rawAsset = fileHandleRawAssetMap.get(file);
				fileHandleGameAssetObjectMap.remove(file);

				FileHandle oldMeta = AssetImporter.getMetadataHandleFor(file);

				oldMeta.moveTo(destination);
				file.moveTo(destination);

				//Lets check to see if its a tls for special case
				if (file.extension().equals("tls")) {
					//We need to move the .p too
					FileHandle pFile = file.parent().child(file.nameWithoutExtension() + ".p");
					if (pFile.exists()) {
						//Copy this too,
						AssetImporter.moveFile(pFile, destination, false);
					}
				}


				FileHandle newHandle = destination.child(file.name());

				AssetPathChanged assetPathChanged = Notifications.obtainEvent(AssetPathChanged.class);
				assetPathChanged.oldRelativePath = relative(file);
				assetPathChanged.newRelativePath = relative(newHandle);
				Notifications.fireEvent(assetPathChanged);

				fileHandleRawAssetMap.put(newHandle, rawAsset);

				rawAsset.handle = newHandle;

				for (GameAsset gameAssetReference : rawAsset.gameAssetReferences) {
					gameAssetReference.setUpdated();
				}

			} else {
				//Its a rename
				if (fileHandleRawAssetMap.containsKey(file)) {
					RawAsset rawAsset = fileHandleRawAssetMap.get(file);
					fileHandleRawAssetMap.remove(file);

					FileHandle oldMeta = AssetImporter.getMetadataHandleFor(file);

					oldMeta.moveTo(destination.parent().child(destination.name() + ".meta"));
					file.moveTo(destination);

					//Lets check to see if its a tls for special case
					if (file.extension().equals("tls")) {
						//We need to move the .p too
						FileHandle pFile = file.parent().child(file.nameWithoutExtension() + ".p");
						if (pFile.exists()) {
							//Copy this too,
							AssetImporter.moveFile(pFile, destination, false);
						}
					}


					AssetPathChanged assetPathChanged = Notifications.obtainEvent(AssetPathChanged.class);
					assetPathChanged.oldRelativePath = relative(file);
					assetPathChanged.newRelativePath = relative(destination);
					Notifications.fireEvent(assetPathChanged);

					if (isRootGameResource(rawAsset)) {
						//We need to update the game assets identifier
						String gameAssetIdentifierFromRawAsset = getGameAssetIdentifierFromRawAsset(rawAsset);
						GameAssetType typeFromExtension = GameAssetType.getAssetTypeFromExtension(rawAsset.handle.extension());

						GameAsset<?> assetForIdentifier = getAssetForIdentifier(gameAssetIdentifierFromRawAsset, typeFromExtension);

						if (assetForIdentifier != null) {
							GameAsset<?> removedGameAsset = identifierGameAssetMap.get(assetForIdentifier.type).remove(gameAssetIdentifierFromRawAsset);
							putAssetForIdentifier(destination.nameWithoutExtension(), removedGameAsset.type, removedGameAsset);

							removedGameAsset.setUpdated();
						} else {
							System.err.println("No game asset found for identifier " + gameAssetIdentifierFromRawAsset);
						}
					}

					fileHandleRawAssetMap.put(destination, rawAsset);

					rawAsset.handle = destination;

					for (GameAsset gameAssetReference : rawAsset.gameAssetReferences) {
						gameAssetReference.setUpdated();
					}

				} else {
					System.out.println("We moved something we were not tracking");
					collectRawResourceFromDirectory(file, true);
				}
			}

		}

		if (checkGameAssets) {
			checkAllGameAssetCreation();
		}
	}


	private boolean isRootGameResource (RawAsset rawAsset) {
		GameAssetType assetTypeFromExtension = GameAssetType.getAssetTypeFromExtension(rawAsset.handle.extension());
		return assetTypeFromExtension.isRootGameAsset();
	}

}
