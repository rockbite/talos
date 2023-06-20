package com.talosvfx.talos.editor.addons.scene.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasSprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.esotericsoftware.spine.SkeletonBinary;
import com.esotericsoftware.spine.SkeletonData;
import com.talosvfx.talos.editor.addons.scene.apps.routines.RoutineEditorApp;
import com.talosvfx.talos.editor.addons.scene.apps.routines.RoutineStage;
import com.talosvfx.talos.editor.addons.scene.events.*;
import com.talosvfx.talos.editor.addons.scene.events.meta.MetaDataReloadedEvent;
import com.talosvfx.talos.editor.addons.scene.events.explorer.DirectoryMovedEvent;
import com.talosvfx.talos.editor.nodes.NodeWidget;
import com.talosvfx.talos.editor.nodes.widgets.AbstractWidget;
import com.talosvfx.talos.editor.nodes.widgets.GameAssetWidget;
import com.talosvfx.talos.editor.notifications.events.ProjectUnloadEvent;
import com.talosvfx.talos.editor.serialization.EmitterData;
import com.talosvfx.talos.editor.wrappers.ModuleWrapper;
import com.talosvfx.talos.runtime.RuntimeContext;
import com.talosvfx.talos.runtime.assets.AMetadata;
import com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter;
import com.talosvfx.talos.editor.data.RoutineStageData;
import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.Observer;
import com.talosvfx.talos.editor.notifications.events.ProjectLoadedEvent;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.project2.apps.ParticleNodeEditorApp;
import com.talosvfx.talos.editor.project2.savestate.GlobalSaveStateSystem;
import com.talosvfx.talos.editor.serialization.VFXProjectData;
import com.talosvfx.talos.editor.serialization.VFXProjectSerializer;
import com.talosvfx.talos.runtime.assets.BaseAssetRepository;
import com.talosvfx.talos.runtime.assets.GameAssetExportStructure;
import com.talosvfx.talos.runtime.assets.GameAssetsExportStructure;
import com.talosvfx.talos.runtime.assets.meta.DirectoryMetadata;
import com.talosvfx.talos.runtime.assets.meta.ScriptMetadata;
import com.talosvfx.talos.runtime.assets.meta.SpineMetadata;
import com.talosvfx.talos.runtime.assets.meta.SpriteMetadata;
import com.talosvfx.talos.runtime.graphics.NineSlice;
import com.talosvfx.talos.runtime.maps.TilePaletteData;
import com.talosvfx.talos.runtime.scene.GameObjectContainer;
import com.talosvfx.talos.runtime.scene.components.AComponent;
import com.talosvfx.talos.runtime.utils.NamingUtils;
import com.talosvfx.talos.editor.utils.Toasts;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.runtime.assets.GameResourceOwner;
import com.talosvfx.talos.runtime.assets.RawAsset;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.Prefab;
import com.talosvfx.talos.runtime.scene.Scene;
import com.talosvfx.talos.runtime.scene.components.MapComponent;
import com.talosvfx.talos.runtime.scene.components.ScriptComponent;
import com.talosvfx.talos.runtime.utils.TempHackUtil;
import com.talosvfx.talos.runtime.vfx.ParticleEffectDescriptor;
import com.talosvfx.talos.runtime.vfx.serialization.ExportData;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.talosvfx.talos.editor.layouts.LayoutGrid.LayoutJsonStructure;

public class AssetRepository extends BaseAssetRepository implements Observer {

	private static final Logger logger = LoggerFactory.getLogger(AssetRepository.class);
	public static final AssetNameFieldFilter ASSET_NAME_FIELD_FILTER = new AssetNameFieldFilter();

	private ObjectMap<GameAssetType, ObjectMap<String, GameAsset<?>>> identifierGameAssetMap = new ObjectMap<>();
	private ObjectMap<GameAssetType, ObjectMap<UUID, GameAsset<?>>> uniqueIdentifierGameAssetMap = new ObjectMap<>();
	private ObjectMap<GameAsset<AtlasSprite>, NineSlice> patchCache = new ObjectMap<>();
	private ObjectSet<FileHandle> newFilesSeen = new ObjectSet<>();


	public <T> GameAsset<T> getAssetForUniqueIdentifier (UUID uuid, GameAssetType type) {
		if (uniqueIdentifierGameAssetMap.containsKey(type)) {
			if (uniqueIdentifierGameAssetMap.get(type).containsKey(uuid)) {
				return (GameAsset<T>)uniqueIdentifierGameAssetMap.get(type).get(uuid);
			}
		}
		GameAsset<T> brokenAsset = new GameAsset<>(uuid.toString(), type);
		brokenAsset.setBroken(new Exception("No asset found"));
		brokenAsset.setNonFound(true);
		return brokenAsset;
	}

	public <T> GameAsset<T> getAssetForIdentifier (String identifier, GameAssetType type) {
		if (identifierGameAssetMap.containsKey(type)) {
			if (identifierGameAssetMap.get(type).containsKey(identifier)) {
				return (GameAsset<T>)identifierGameAssetMap.get(type).get(identifier);
			}
		}
		GameAsset<T> brokenAsset = new GameAsset<>(identifier, type);
		brokenAsset.setBroken(new Exception("No asset found"));
		brokenAsset.setNonFound(true);
		return brokenAsset;
	}

	@Override
	public NineSlice obtainNinePatch (GameAsset<AtlasSprite> gameAsset) {
		if (patchCache.containsKey(gameAsset) && false) { //something better, maybe hash on pixel size + texture for this
			return patchCache.get(gameAsset);
		} else {
			final SpriteMetadata metadata = (SpriteMetadata)gameAsset.getRootRawAsset().metaData;
			final NineSlice patch = new NineSlice(gameAsset.getResource(), metadata.borderData[0], metadata.borderData[1], metadata.borderData[2], metadata.borderData[3]);
			patch.scale(1 / metadata.pixelsPerUnit, 1 / metadata.pixelsPerUnit); // fix this later
			patchCache.put(gameAsset, patch);
			return patch;
		}
	}

	@EventHandler
	public void onSpritePixelPerUnitUpdateEvent (SpritePixelPerUnitUpdateEvent event) {
		final SpriteMetadata metadata = event.getSpriteMetadata();
		for (ObjectMap.Entry<GameAsset<AtlasSprite>, NineSlice> gameAssetNinePatchEntry : patchCache) {
			if (gameAssetNinePatchEntry.key.getRootRawAsset().metaData.equals(metadata)) {
				final NineSlice patch = new NineSlice(gameAssetNinePatchEntry.key.getResource(), metadata.borderData[0], metadata.borderData[1], metadata.borderData[2], metadata.borderData[3]);
				final float scale = 1 / metadata.pixelsPerUnit;
				patch.scale(scale, scale);
				patchCache.put(gameAssetNinePatchEntry.key, patch);
				break;
			}
		}
	}

	private <T> void putAssetForIdentifier (String identifier, GameAssetType type, GameAsset<T> asset) {
		if (!identifierGameAssetMap.containsKey(type)) {
			identifierGameAssetMap.put(type, new ObjectMap<>());
		}
		identifierGameAssetMap.get(type).put(identifier, asset);
	}

	private <T> void putAssetForUniqueIdentifier (UUID uuid, GameAssetType type, GameAsset<T> asset) {
		if (!uniqueIdentifierGameAssetMap.containsKey(type)) {
			uniqueIdentifierGameAssetMap.put(type, new ObjectMap<>());
		}
		uniqueIdentifierGameAssetMap.get(type).put(uuid, asset);
	}

	public void reloadMetaData (AMetadata metadata) {
		FileHandle metadataHandleFor = AssetImporter.getMetadataHandleFor(metadata.link.handle);
		JsonValue jsonValue = new JsonReader().parse(metadataHandleFor);
		metadata.read(json, jsonValue);

		MetaDataReloadedEvent metaDataReloadedEvent = Notifications.obtainEvent(MetaDataReloadedEvent.class);
		metaDataReloadedEvent.setMetadata(metadata);
		Notifications.fireEvent(metaDataReloadedEvent);
	}

	public <T> GameAsset<T> getAssetForResource (T resource) {
		ObjectMap.Entries<GameAssetType, ObjectMap<UUID, GameAsset<?>>> iterator = uniqueIdentifierGameAssetMap.iterator();
		while (iterator.hasNext()) {
			ObjectMap.Entry<GameAssetType, ObjectMap<UUID, GameAsset<?>>> next = iterator.next();

			ObjectMap<UUID, GameAsset<?>> mapForUniqueIdentifier = next.value;
			ObjectMap.Entries<UUID, GameAsset<?>> assetsForUniqueIdentifier = mapForUniqueIdentifier.iterator();

			for (ObjectMap.Entry<UUID, GameAsset<?>> gameAssetEntry : assetsForUniqueIdentifier) {
				GameAsset<?> asset = gameAssetEntry.value;
				if (asset.getResource() == resource) {
					return (GameAsset<T>)asset;
				}
			}
		}
		return null;
	}

	public FileHandle copySampleSceneToProject (FileHandle preferredDestination) {
		FileHandle originalScene = Gdx.files.internal("addons/scene/missing/New Scene.scn");

		return AssetRepository.getInstance().copyRawAsset(originalScene, preferredDestination);
	}

	public <T> GameAsset<T> findFirstOfType (GameAssetType gameAssetType) {
		ObjectMap<UUID, GameAsset<?>> entries = uniqueIdentifierGameAssetMap.get(gameAssetType);
		if (entries != null && entries.size > 0) {
			for (ObjectMap.Entry<UUID, GameAsset<?>> entry : entries) {
				return (GameAsset<T>)entry.value;
			}
		}
		return null;
	}

	static class DataMaps {
		private ObjectMap<FileHandle, GameAsset> fileHandleGameAssetObjectMap = new ObjectMap<>();
		private ObjectMap<UUID, RawAsset> uuidRawAssetMap = new ObjectMap<>();
		private ObjectMap<FileHandle, RawAsset> fileHandleRawAssetMap = new ObjectMap<>();

		void putFileHandleGameAsset (FileHandle handle, GameAsset<?> gameAsset) {
			this.fileHandleGameAssetObjectMap.put(handle, gameAsset);
		}

		void clearFileHandleGameAssets () {
			fileHandleGameAssetObjectMap.clear();
			logger.info("Cleared file handles in file handle game asset map.");
		}

		void putUUIDRawAsset (UUID uuid, RawAsset rawAsset) {
			uuidRawAssetMap.put(uuid, rawAsset);
		}

		void clearUUIDRawAssets () {
			uuidRawAssetMap.clear();

			logger.info("Cleared uuids for raw asset map.");
		}

		void putFileHandleRawAsset (FileHandle handle, RawAsset rawAsset) {
			fileHandleRawAssetMap.put(handle, rawAsset);
		}

		void clearFileHandleRawAssets () {
			fileHandleRawAssetMap.clear();

			logger.info("Cleared file handle raw assets map.");
		}

		public GameAsset removeFileHandleGameAssetObjectMap (FileHandle handle) {
			System.out.println("Removing file handle game asset " + handle.path());
			return fileHandleGameAssetObjectMap.remove(handle);
		}

		public RawAsset removeFileHandleRawAsset (FileHandle handle) {
			System.out.println("Removing file handle raw asset " + handle.path());
			return fileHandleRawAssetMap.remove(handle);
		}

		public RawAsset removeUUIDRawAsset (UUID uuid) {

			System.out.println("Removing uuid raw asset " + uuid.toString());
			return uuidRawAssetMap.remove(uuid);
		}
	}

	private DataMaps dataMaps = new DataMaps();

	private FileHandle assetsRoot;
	private Json json;

	public TextureRegion brokenTextureRegion;

	static AssetRepository instance;

	public static AssetRepository getInstance () {
		if (instance == null)
			init();
		return instance;
	}

	public static void init () {
		AssetRepository assetRepository = new AssetRepository();
		Notifications.registerObserver(assetRepository);
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
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run () {
				//todo scripts
//				loadChangesFromScripts(AssetRepository.this::fileVisit);
			}
		});
	}

	public void unloadAssets () {
		this.assetsRoot = null;

		identifierGameAssetMap.clear();
		logger.info("Cleared identifiers in identifier game asset map.");

		uniqueIdentifierGameAssetMap.clear();
		logger.info("Cleared uuids in unique identifier game asset map.");

		newFilesSeen.clear();

		// dispose all game assets
		for (ObjectMap.Entry<FileHandle, GameAsset> entry : dataMaps.fileHandleGameAssetObjectMap) {
			FileHandle key = entry.key;
			GameAsset<?> value = entry.value;

			if (key.isDirectory())
				continue;

			disposeGameAssetForType(value);
		}

		// clear data maps
		dataMaps.clearFileHandleGameAssets();
		dataMaps.clearUUIDRawAssets();
		dataMaps.clearFileHandleRawAssets();
	}

	private void loadChangesFromScripts (Function<Path, FileVisitResult> function) {

		FileHandle exportedScriptsFolderHandle = getExportedScriptsFolderHandle();
		if (!exportedScriptsFolderHandle.exists()) {
			return;
		}
		try {
			Path exportPath = exportedScriptsFolderHandle.file().toPath();
			Files.walkFileTree(exportPath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile (Path file, BasicFileAttributes attrs) throws IOException {
					return function.apply(file);
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public FileVisitResult fileVisit (Path dir) {
		File file = dir.toFile();
		if (file.isDirectory()) {
			return FileVisitResult.CONTINUE;
		}
		FileHandle fileHandle = new FileHandle(file);
		if (fileHandle.extension().equals("meta")) {
			return FileVisitResult.CONTINUE;
		}

		if (fileHandle.extension().equals("ts")) {
			// Found a script
			Notifications.fireEvent(Notifications.obtainEvent(ScriptFileChangedEvent.class).set(StandardWatchEventKinds.ENTRY_MODIFY, fileHandle));
		}
		return FileVisitResult.CONTINUE;
	}

	private void checkAllGameAssetCreation () { //raws
		checkGameAssetCreation(GameAssetType.SPRITE);
		checkGameAssetCreation(GameAssetType.SCRIPT);
		checkGameAssetCreation(GameAssetType.ROUTINE);
		checkGameAssetCreation(GameAssetType.ATLAS);
		checkGameAssetCreation(GameAssetType.SOUND);

		checkGameAssetCreation(GameAssetType.SKELETON);

		checkGameAssetCreation(GameAssetType.VFX);
		checkGameAssetCreation(GameAssetType.VFX_OUTPUT);
		checkGameAssetCreation(GameAssetType.PREFAB);
		checkGameAssetCreation(GameAssetType.SCENE);

		checkGameAssetCreation(GameAssetType.TILE_PALETTE);

		checkGameAssetCreation(GameAssetType.LAYOUT_DATA);

		newFilesSeen.clear();
	}

	private void checkGameAssetCreation (GameAssetType type) {
		//We need to do multiple passes here for dependent assets

		for (ObjectMap.Entry<FileHandle, RawAsset> entry : dataMaps.fileHandleRawAssetMap) {
			FileHandle key = entry.key;
			RawAsset value = entry.value;

			if (key.isDirectory())
				continue;
			if (!newFilesSeen.contains(key)) {
				continue;
			}

			try {
				GameAssetType assetTypeFromExtension = GameAssetType.getAssetTypeFromExtension(key.extension());
				if (type != assetTypeFromExtension)
					continue;

				if (assetTypeFromExtension.isRootGameAsset()) {
					createGameAsset(key, value);
				}
			} catch (GameAssetType.NoAssetTypeException e) {
				//Its not an asset
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

	public void reloadGameAsset (GameAsset gameAssetReference) {
		RawAsset rootRawAsset = gameAssetReference.getRootRawAsset();
		String gameAssetIdentifier = getGameAssetIdentifierFromRawAsset(rootRawAsset);

		try {
			GameAssetType assetTypeFromExtension = GameAssetType.getAssetTypeFromExtension(rootRawAsset.handle.extension());

			GameAsset gameAsset = createOrUpdateGameAssetForType(assetTypeFromExtension, gameAssetIdentifier, rootRawAsset, false, gameAssetReference);
			gameAssetReference.setResourcePayload(gameAsset.getResource());
			gameAssetReference.setUpdated();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void reloadGameAssetFromString (GameAsset gameAssetReference, String asSTring) {
		RawAsset rootRawAsset = gameAssetReference.getRootRawAsset();
		String gameAssetIdentifier = getGameAssetIdentifierFromRawAsset(rootRawAsset);


		String userHome = System.getProperty("user.home");
		FileHandle talos = Gdx.files.absolute(userHome).child("Talos");
		FileHandle tempDir = talos.child(".temp");
		tempDir.mkdirs();

		FileHandle temp = tempDir.child("tempForReload.txt");
		temp.writeString(asSTring, false);

		RawAsset rawAsset = new RawAsset(temp);

		try {
			GameAssetType assetTypeFromExtension = GameAssetType.getAssetTypeFromExtension(rootRawAsset.handle.extension());

			GameAsset gameAsset = createOrUpdateGameAssetForType(assetTypeFromExtension, gameAssetIdentifier, rawAsset, false, gameAssetReference);
			gameAssetReference.setResourcePayload(gameAsset.getResource());
			gameAssetReference.setUpdated();

		} catch (Exception e) {
			e.printStackTrace();
		}

		temp.delete();
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
	public void exportToFile (AssetRepositoryCatalogueExportOptions settings, boolean isOptimized) { //todo
		//Go over all entities, go over all components. If component has a game resource, we mark it for export

		ObjectSet<GameAsset<?>> gameAssetsToExport = new ObjectSet<>();
		GameAssetsExportStructure gameAssetExportStructure = new GameAssetsExportStructure();

		gameAssetExportStructure.sceneData = RuntimeContext.getInstance().sceneData;

		if (settings.getExportPathHandle().child("assetExport.json").exists()) {
			Toasts.getInstance().showInfoToast("Cleaning export directory");
			// remove everything except assetExport.json, because we
			// need it asset packing optimization
			FileHandle[] toRemove = settings.getExportPathHandle().list((file, s) -> !s.equals("assetExport.json"));
			for (FileHandle handle : toRemove) {
				handle.delete();
			}
		}

		if (settings.isForceExportAll()) {

			//Gather all assets and export
			Toasts.getInstance().showInfoToast("Exporting all");

			Predicate<GameAsset<?>> forceAllPredicate = new Predicate<GameAsset<?>>() {
				@Override
				public boolean evaluate (GameAsset<?> gameAsset) {
					return true;
				}
			};

			exportGameAsset(settings, GameAssetType.SPRITE, forceAllPredicate, gameAssetsToExport);
			exportGameAsset(settings, GameAssetType.ATLAS, forceAllPredicate, gameAssetsToExport);
			exportGameAsset(settings, GameAssetType.SCRIPT, forceAllPredicate, gameAssetsToExport);
			exportGameAsset(settings, GameAssetType.ROUTINE, forceAllPredicate, gameAssetsToExport);
			exportGameAsset(settings, GameAssetType.SOUND, forceAllPredicate, gameAssetsToExport);
			exportGameAsset(settings, GameAssetType.SKELETON, forceAllPredicate, gameAssetsToExport);
			exportGameAsset(settings, GameAssetType.VFX, forceAllPredicate, gameAssetsToExport);
			exportGameAsset(settings, GameAssetType.PREFAB, forceAllPredicate, gameAssetsToExport);
			exportGameAsset(settings, GameAssetType.SCENE, forceAllPredicate, gameAssetsToExport);
			exportGameAsset(settings, GameAssetType.TILE_PALETTE, forceAllPredicate, gameAssetsToExport);
			exportGameAsset(settings, GameAssetType.LAYOUT_DATA, forceAllPredicate, gameAssetsToExport);
		} else {
			logger.info("todo check all  other cases");
		}

		//Make deep copies
		ObjectSet<GameAsset<?>> copiedAssets = new ObjectSet<>();
		for (GameAsset<?> gameAsset : gameAssetsToExport) {
			copiedAssets.add(gameAsset.copy());
		}
		gameAssetsToExport.clear();
		gameAssetsToExport.addAll(copiedAssets);

		if (isOptimized) {
				startOptimizedExport(gameAssetsToExport, settings, gameAssetExportStructure, new Runnable(){
					@Override
					public void run () {
						exportToTargetDir(gameAssetsToExport, settings, gameAssetExportStructure);

						FileHandle assetRepoExportFile = settings.getExportPathHandle().child("assetExport.json");
						assetRepoExportFile.writeString(json.toJson(gameAssetExportStructure), false);

						Toasts.getInstance().showInfoToast("Optimized export completed");

					}
				});

		} else {
			exportToTargetDir(gameAssetsToExport, settings, gameAssetExportStructure);

			FileHandle assetRepoExportFile = settings.getExportPathHandle().child("assetExport.json");
			assetRepoExportFile.writeString(json.toJson(gameAssetExportStructure), false);
		}


	}

	private void startOptimizedExport (ObjectSet<GameAsset<?>> gameAssetsToExport, AssetRepositoryCatalogueExportOptions settings, GameAssetsExportStructure gameAssetExportStructure, Runnable runnable) {
		Toasts.getInstance().showInfoToast("Starting optimized export in background");

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run () {
				RepositoryOptimizer.startProcess(gameAssetsToExport, gameAssetExportStructure, settings, runnable);
			}
		});
		thread.start();
	}


	private void exportToTargetDir (ObjectSet<GameAsset<?>> gameAssetsToExport, AssetRepositoryCatalogueExportOptions settings, GameAssetsExportStructure gameAssetExportStructure) {
		for (GameAsset<?> gameAsset : gameAssetsToExport) {

			Array<RawAsset> dependentRawAssets = gameAsset.dependentRawAssets;

			GameAssetType type = gameAsset.type;

			FileHandle exportPathHandle = settings.getExportPathHandle();
			FileHandle destinationForChildDirectory = exportPathHandle.child(type.name());

			if (!destinationForChildDirectory.exists()) {
				destinationForChildDirectory.mkdirs();
			}

			GameAssetExportStructure assetExportStructure = new GameAssetExportStructure();
			assetExportStructure.identifier = gameAsset.nameIdentifier;
			assetExportStructure.uuid = gameAsset.getRootRawAsset().metaData.uuid.toString();
			assetExportStructure.type = gameAsset.type;
			assetExportStructure.dependentGameAssets.addAll(dependentGameAssetsToUUIDArray(gameAsset));

			FileHandle metadataHandleFor = AssetImporter.getMetadataHandleFor(gameAsset.getRootRawAsset().handle);
			String metaFileName = metadataHandleFor.name();

			boolean primaryFile = true;
			for (RawAsset dependentRawAsset : dependentRawAssets) {

				//We need folder structure
				FileHandle projectDir = SharedResources.currentProject.getProjectDir();
				String relativeFromRootDir = getRelativePathFromRoot(projectDir, dependentRawAsset.handle);
				FileHandle dirToCopyInto = destinationForChildDirectory.child(relativeFromRootDir);
				if (!dirToCopyInto.exists()) {
					dirToCopyInto.mkdirs();
				}

				//Conversions
				boolean converted = false;
				if (primaryFile) {
					if (type == GameAssetType.VFX) {
						//Lets convert vfx file handle if this is the one
						GameAsset<VFXProjectData> castedVfxGameAsset = (GameAsset<VFXProjectData>)gameAsset;
						ExportData exportData = VFXProjectSerializer.exportTLSDataToP(castedVfxGameAsset.getResource());
						String fileToWrite = VFXProjectSerializer.writeTalosPExport(exportData);
						String newFileName = dependentRawAsset.handle.nameWithoutExtension() + ".p";
						dirToCopyInto.child(newFileName).writeString(fileToWrite, false);
						converted = true;

						assetExportStructure.relativePathsOfRawFiles.add(relativeFromRootDir + newFileName);

						metaFileName = newFileName + ".meta";
					}
				}

				if (!converted) {
					if (dependentRawAsset.shouldExport) {
						dependentRawAsset.handle.copyTo(dirToCopyInto);
					}
					assetExportStructure.relativePathsOfRawFiles.add(relativeFromRootDir + dependentRawAsset.handle.name());
				}

				if (primaryFile) {
					dirToCopyInto.child(metaFileName).writeString(json.prettyPrint(gameAsset.getRootRawAsset().metaData), false);
				}

				primaryFile = false;
			}

			gameAssetExportStructure.gameAssets.add(assetExportStructure);

		}

		GameAssetExportStructure value = new GameAssetExportStructure();
		value.type = GameAssetType.SPRITE;
		value.identifier = "missing";
		value.uuid = missingUUID.toString();
		gameAssetExportStructure.gameAssets.add(value);
	}

	private ObjectSet<String> dependentGameAssetsToUUIDArray (GameAsset<?> gameAsset) {
		ObjectSet<String> uuids = new ObjectSet<>();
		for (GameAsset<?> dependentGameAsset : gameAsset.dependentGameAssets) {
			uuids.add(dependentGameAsset.getRootRawAsset().metaData.uuid.toString());
		}

		//Are we something that does shit dynamically? Like Scene/Prefab?

		if (gameAsset.type == GameAssetType.PREFAB || gameAsset.type == GameAssetType.SCENE) {
			GameAsset<GameObjectContainer> castedGameAsset = (GameAsset<GameObjectContainer>)gameAsset;
			GameObjectContainer container = castedGameAsset.getResource();
			GameObject selfObject = container.getSelfObject();
			collectDependentGameResources(selfObject, uuids);
		}

		if (gameAsset.type == GameAssetType.ROUTINE) {
			GameAsset<RoutineStageData> routineStageDataGameAsset = (GameAsset<RoutineStageData>)gameAsset;
			Array<NodeWidget> nodes = routineStageDataGameAsset.getResource().getNodes();
			if (nodes.size == 0) {
				RoutineEditorApp routineEditorApp = new RoutineEditorApp();
				routineEditorApp.updateForGameAsset(routineStageDataGameAsset);

				routineEditorApp.onRemove();
			}
			for (NodeWidget node : nodes) {
				ObjectMap<String, AbstractWidget> widgetMap = node.getWidgetMap();
				for (ObjectMap.Entry<String, AbstractWidget> stringAbstractWidgetEntry : widgetMap) {
					AbstractWidget widget = stringAbstractWidgetEntry.value;
					if (widget instanceof GameAssetWidget) {
						GameAssetWidget<?> gameAssetWidget = (GameAssetWidget<?>) widget;
						GameAsset<?> value = gameAssetWidget.getValue();
						uuids.add(value.getRootRawAsset().metaData.uuid.toString());
					}
				}
			}
		}

		return uuids;
	}

	private void collectDependentGameResources (GameObject selfObject, ObjectSet<String> uuids) {
		for (AComponent component : selfObject.getComponents()) {
			if (component instanceof GameResourceOwner) {
				GameResourceOwner gameResourceOwner = (GameResourceOwner) component;
				GameAsset gameResource = gameResourceOwner.getGameResource();
				if (gameResource.isBroken()) {
					uuids.add(missingUUID.toString());
				} else {
					uuids.add(gameResource.getRootRawAsset().metaData.uuid.toString());
				}
			}
		}
		Array<GameObject> gameObjects = selfObject.getGameObjects();
		for (int i = 0; i < gameObjects.size; i++) {
			collectDependentGameResources(gameObjects.get(i), uuids);
		}
	}

	private boolean copyMetaIfExists (FileHandle handle, FileHandle dirToCopyInto) {
		FileHandle meta = handle.parent().child(handle.name() + ".meta");
		if (meta.exists()) {
			meta.copyTo(dirToCopyInto);
			return true;
		}
		return false;
	}

	private String getRelativePathFromRoot (FileHandle projectDir, FileHandle pathInsideProjectDir) {
		String path = projectDir.path();
		String childPath = pathInsideProjectDir.parent().path();
		String[] splits = childPath.split(path);
		if (splits.length != 2) {
			return "/";
		}
		String relativePath = splits[1];
		if (!relativePath.endsWith("/")) {
			relativePath += "/";
		}
		return relativePath;
	}

	private void exportGameAsset (AssetRepositoryCatalogueExportOptions settings, GameAssetType gameAssetType, Predicate<GameAsset<?>> acceptancePredicate, ObjectSet<GameAsset<?>> assetsToExportSet) {
		Array<GameAsset<Object>> assetsForType = getAssetsForType(gameAssetType);
		for (GameAsset<?> objectGameAsset : assetsForType) {
			if (gameAssetType == GameAssetType.VFX) {
				GameAsset<VFXProjectData> vfxGameAsset = (GameAsset<VFXProjectData>)objectGameAsset;

				VFXProjectData resource = vfxGameAsset.getResource();
				for (EmitterData emitter : resource.getEmitters()) {
					for (ModuleWrapper module : emitter.modules) {
						if (module.getModule() instanceof GameResourceOwner) {
							GameAsset<?> dependentAsset = ((GameResourceOwner<?>)module.getModule()).getGameResource();
							if (!objectGameAsset.dependentGameAssets.contains(dependentAsset, false)) {
								objectGameAsset.dependentGameAssets.add(dependentAsset);
							}
						}
					}
				}

			}

			if (acceptancePredicate.evaluate(objectGameAsset)) {
				assetsToExportSet.add(objectGameAsset);
				assetsToExportSet.addAll(objectGameAsset.dependentGameAssets);
			}
		}

	}

	private void findAllPrefabs (FileHandle assets, ObjectSet<TypeIdentifierPair> identifiersBeingUsedByComponents) {
		FileHandle[] list = assets.list();
		for (FileHandle handle : list) {
			if (handle.isDirectory()) {
				findAllPrefabs(handle, identifiersBeingUsedByComponents);
			} else {
				if (handle.extension().equals("prefab")) {
					//Get the raw asset
					GameAsset<Prefab> gameAsset = dataMaps.fileHandleGameAssetObjectMap.get(handle);
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

	private UUID getGameAssetUniqueIdentifierFromRawAsset (RawAsset asset) {
		return asset.metaData.uuid;
	}

	private void createGameAsset (FileHandle key, RawAsset value) {
		String gameAssetIdentifier = getGameAssetIdentifierFromRawAsset(value);
		UUID gameAssetUniqueIdentifier = getGameAssetUniqueIdentifierFromRawAsset(value);

		GameAssetType assetTypeFromExtension = null;
		try {
			assetTypeFromExtension = GameAssetType.getAssetTypeFromExtension(key.extension());
		} catch (GameAssetType.NoAssetTypeException e) {
			throw new RuntimeException(e);
		}

		GameAsset<Object> exitingObject = getAssetForUniqueIdentifier(gameAssetUniqueIdentifier, assetTypeFromExtension);
		if (exitingObject != null && !exitingObject.isNonFound()) {
			//Should just reload the resource not create new game asset
			GameAsset gameAsset = createOrUpdateGameAssetForType(assetTypeFromExtension, gameAssetIdentifier, value, true, exitingObject);

			gameAsset.setUpdated();
			return;
		}

		GameAsset gameAsset = createOrUpdateGameAssetForType(assetTypeFromExtension, gameAssetIdentifier, value, true);

		if (gameAsset == null)
			return;

		putAssetForIdentifier(gameAssetIdentifier, assetTypeFromExtension, gameAsset);
		putAssetForUniqueIdentifier(gameAssetUniqueIdentifier, assetTypeFromExtension, gameAsset);
		dataMaps.putFileHandleGameAsset(key, gameAsset);
	}

	public FileHandle copySampleParticleToProject (FileHandle preferredDestination) {
		FileHandle originalTls = Gdx.files.internal("addons/scene/missing/sample.tls");
		FileHandle imageRequired = Gdx.files.internal("addons/scene/missing/white.png");

		if (!preferredDestination.child(imageRequired.name()).exists()) {
			AssetRepository.getInstance().copyRawAsset(imageRequired, preferredDestination);
		} else {
			System.out.println("Ignoring copying white, since we have it already");
		}

		return AssetRepository.getInstance().copyRawAsset(originalTls, preferredDestination);
	}

	public GameAsset<?> createOrUpdateGameAssetForType (GameAssetType assetTypeFromExtension, String gameAssetIdentifier, RawAsset value, boolean createLinks) {
		return createOrUpdateGameAssetForType(assetTypeFromExtension, gameAssetIdentifier, value, createLinks, null);
	}

	public GameAsset<?> createOrUpdateGameAssetForType (GameAssetType assetTypeFromExtension, String gameAssetIdentifier, RawAsset value, boolean createLinks, @Null GameAsset<?> in) {
		if (!assetTypeFromExtension.isRootGameAsset()) {
			throw new GdxRuntimeException("Trying to load a game asset from a non root asset");
		}

		GameAsset<?> gameAssetOut = in;

		try {
			switch (assetTypeFromExtension) {
			case SPRITE:

				if (gameAssetOut == null) {
					GameAsset<AtlasSprite> textureGameAsset = new GameAsset<>(gameAssetIdentifier, assetTypeFromExtension);
					gameAssetOut = textureGameAsset;

					if (createLinks) {
						value.gameAssetReferences.add(textureGameAsset);
						textureGameAsset.dependentRawAssets.add(value);
					}
				}

				Texture resourcePayload = new Texture(value.handle);
				((GameAsset<AtlasSprite>)gameAssetOut).setResourcePayload(new AtlasSprite(new TextureAtlas.AtlasRegion(new TextureRegion(resourcePayload))));

				break;
			case ATLAS:

				TextureAtlas.TextureAtlasData textureAtlasData = new TextureAtlas.TextureAtlasData(value.handle, value.handle.parent(), false);

				if (gameAssetOut == null) {
					GameAsset<TextureAtlas> textureAtlasGameAsset = new GameAsset<>(gameAssetIdentifier, assetTypeFromExtension);
					gameAssetOut = textureAtlasGameAsset;

					if (createLinks) {
						value.gameAssetReferences.add(textureAtlasGameAsset);
						textureAtlasGameAsset.dependentRawAssets.add(value);

						for (TextureAtlas.TextureAtlasData.Page page : textureAtlasData.getPages()) {
							FileHandle textureFile = page.textureFile;
							if (!dataMaps.fileHandleRawAssetMap.containsKey(textureFile)) {
								throw new GdxRuntimeException("Corruption, texture file does not exist" + textureFile);
							}

							RawAsset rawAssetForPage = dataMaps.fileHandleRawAssetMap.get(textureFile);

							rawAssetForPage.gameAssetReferences.add(textureAtlasGameAsset);
							textureAtlasGameAsset.dependentRawAssets.add(rawAssetForPage);
						}
					}
				}

				TextureAtlas atlas = new TextureAtlas(textureAtlasData);
				((GameAsset<TextureAtlas>)gameAssetOut).setResourcePayload(atlas);

				break;

			case SKELETON:

				//Gotta try load the atlas
				String skeleName = value.handle.nameWithoutExtension();
				FileHandle atlasFile = value.handle.parent().child(skeleName + ".atlas");

				//We gotta find reference to the atlas asset
				GameAsset<TextureAtlas> textureAtlasGameAsset = getAssetForIdentifier(atlasFile.nameWithoutExtension(), GameAssetType.ATLAS);
				if (textureAtlasGameAsset == null) {
					logger.warn("No atlas found for skeleton {}", skeleName);
					break;
				}


				SkeletonBinary skeletonBinary = new SkeletonBinary(textureAtlasGameAsset.getResource());
				SpineMetadata metaData = (SpineMetadata)value.metaData;
				skeletonBinary.setScale(1f / metaData.pixelsPerUnit);

				SkeletonData skeletonData = skeletonBinary.readSkeletonData(value.handle);

				if (gameAssetOut == null) {
					GameAsset<SkeletonData> skeletonDataGameAsset = new GameAsset<>(gameAssetIdentifier, assetTypeFromExtension);

					if (createLinks) {
						value.gameAssetReferences.add(skeletonDataGameAsset);
						skeletonDataGameAsset.dependentRawAssets.add(value);
					}

					skeletonDataGameAsset.dependentGameAssets.add(textureAtlasGameAsset);
					gameAssetOut = skeletonDataGameAsset;
				}

				((GameAsset<SkeletonData>)gameAssetOut).setResourcePayload(skeletonData);

				break;
			case SOUND:

				if (gameAssetOut == null) {
					GameAsset<Music> musicGameAsset = new GameAsset<>(gameAssetIdentifier, assetTypeFromExtension);
					gameAssetOut = musicGameAsset;

					if (createLinks) {
						value.gameAssetReferences.add(musicGameAsset);
						musicGameAsset.dependentRawAssets.add(value);
					}
				}

				Music music = Gdx.audio.newMusic(value.handle);

				((GameAsset<Music>)gameAssetOut).setResourcePayload(music);

				break;
			case VFX_OUTPUT:

				if (gameAssetOut == null) {

					GameAsset<ParticleEffectDescriptor> particleEffectDescriptorGameAsset = new GameAsset<>(gameAssetIdentifier, assetTypeFromExtension);

					gameAssetOut = particleEffectDescriptorGameAsset;

					if (createLinks) {
						particleEffectDescriptorGameAsset.dependentRawAssets.add(value);
					}

					ParticleEffectDescriptor particleEffectDescriptor = new ParticleEffectDescriptor();
					RawAsset rawAssetPFile = dataMaps.fileHandleRawAssetMap.get(value.handle);

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
				} else {
					GameAsset<ParticleEffectDescriptor> assetToUpdate = ((GameAsset<ParticleEffectDescriptor>)gameAssetOut);

					ParticleEffectDescriptor particleEffectDescriptor = new ParticleEffectDescriptor();
					try {
						RawAsset rawAssetPFile = dataMaps.fileHandleRawAssetMap.get(value.handle);

						particleEffectDescriptor.load(rawAssetPFile.handle);
					} catch (Exception e) {
						System.out.println("Failure to load particle effect");
						throw e;
					}

					assetToUpdate.setResourcePayload(particleEffectDescriptor);

				}

				break;
			case VFX:

				if (gameAssetOut == null) {

					GameAsset<VFXProjectData> vfxProjectDataGameAsset = new GameAsset<>(gameAssetIdentifier, assetTypeFromExtension);

					gameAssetOut = vfxProjectDataGameAsset;

					if (createLinks) {
						vfxProjectDataGameAsset.dependentRawAssets.add(value);
					}

					if (createLinks) {
						value.gameAssetReferences.add(vfxProjectDataGameAsset);
					}
				}

				VFXProjectData projectData = VFXProjectSerializer.readTalosTLSProject(value.handle);
				((GameAsset<VFXProjectData>)gameAssetOut).setResourcePayload(projectData);

				//This is mega hack. Only because we will be making it into DynamicNodeStage later
				ParticleNodeEditorApp app = new ParticleNodeEditorApp();
				app.loadProject(projectData);

				break;
			case SCRIPT:
				if (gameAssetOut == null) {

					GameAsset<String> scriptGameAsset = new GameAsset<>(gameAssetIdentifier, assetTypeFromExtension);
					gameAssetOut = scriptGameAsset;

					if (createLinks) {
						value.gameAssetReferences.add(scriptGameAsset);
						scriptGameAsset.dependentRawAssets.add(value);
					}
				}
				((GameAsset<String>)gameAssetOut).setResourcePayload("ScriptDummy");

				break;
			case ROUTINE:

				if (gameAssetOut == null) {
					GameAsset<RoutineStageData> asset = new GameAsset<RoutineStageData>(gameAssetIdentifier, assetTypeFromExtension) {
						@Override
						public void setUpdated () {
							getResource().setName(getRootRawAsset().handle.nameWithoutExtension());
							super.setUpdated();
						}
					};

					gameAssetOut = asset;

					if (createLinks) {
						value.gameAssetReferences.add(asset);
						asset.dependentRawAssets.add(value);
					}
				}

				RoutineStageData routineStageData = json.fromJson(RoutineStageData.class, TempHackUtil.hackIt(value.handle.readString()));

				routineStageData.setName(value.handle.nameWithoutExtension());

				((GameAsset<RoutineStageData>)gameAssetOut).setResourcePayload(routineStageData);

				break;
			case PREFAB:

				if (gameAssetOut == null) {

					GameAsset<Prefab> prefabGameAsset = new GameAsset<Prefab>(gameAssetIdentifier, assetTypeFromExtension) {
						@Override
						public void setUpdated () {
							getResource().setName(getRootRawAsset().handle.nameWithoutExtension());
							super.setUpdated();
						}
					};
					gameAssetOut = prefabGameAsset;

					if (createLinks) {
						value.gameAssetReferences.add(prefabGameAsset);
						prefabGameAsset.dependentRawAssets.add(value);
					}
				}
				Prefab prefab = new Prefab(value.handle);

				((GameAsset<Prefab>)gameAssetOut).setResourcePayload(prefab);

				break;
			case SCENE:
				if (gameAssetOut == null) {
					GameAsset<Scene> sceneGameAsset = new GameAsset<Scene>(gameAssetIdentifier, assetTypeFromExtension) {
						@Override
						public void setUpdated () {
							getResource().setName(getRootRawAsset().handle.nameWithoutExtension());
							super.setUpdated();
						}
					};
					gameAssetOut = sceneGameAsset;

					if (createLinks) {
						value.gameAssetReferences.add(sceneGameAsset);
						sceneGameAsset.dependentRawAssets.add(value);
					}
				}

				Scene scene = new Scene();
				scene.loadFromHandle(value.handle);

				((GameAsset<Scene>)gameAssetOut).setResourcePayload(scene);

				break;
			case TILE_PALETTE:

				if (gameAssetOut == null) {
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
				} else {
					TilePaletteData paletteData = json.fromJson(TilePaletteData.class, value.handle);
					((GameAsset<TilePaletteData>)gameAssetOut).setResourcePayload(paletteData);
				}
				break;
			case LAYOUT_DATA:
				if (gameAssetOut == null) {
					GameAsset<LayoutJsonStructure> layoutGridGameAsset = new GameAsset<>(gameAssetIdentifier, assetTypeFromExtension);
					gameAssetOut = layoutGridGameAsset;

					JsonReader jsonReader = new JsonReader();
					JsonValue jsonValue = jsonReader.parse(value.handle);
					LayoutJsonStructure layoutJsonStructure = json.readValue(LayoutJsonStructure.class, jsonValue);

					layoutGridGameAsset.setResourcePayload(layoutJsonStructure);

					if (createLinks) {
						value.gameAssetReferences.add(layoutGridGameAsset);
						layoutGridGameAsset.dependentRawAssets.add(value);
					}
				} else {
					JsonReader jsonReader = new JsonReader();
					JsonValue jsonValue = jsonReader.parse(value.handle);
					LayoutJsonStructure layoutJsonStructure = json.readValue(LayoutJsonStructure.class, jsonValue);

					((GameAsset<LayoutJsonStructure>)gameAssetOut).setResourcePayload(layoutJsonStructure);
				}
				break;
			case DIRECTORY:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (gameAssetOut != null) {
				gameAssetOut.setBroken(e);
				System.out.println("Marking asset as broken " + gameAssetOut + " " + value.handle.path());
			}
		}

		return gameAssetOut;
	}

	public void disposeGameAssetForType (@NonNull GameAsset<?> gameAsset) {
		GameAssetType assetTypeFromExtension = gameAsset.type;

		if (!assetTypeFromExtension.isRootGameAsset()) {
			throw new GdxRuntimeException("Trying to dispose a game asset from a non root asset");
		}

		if (gameAsset.isBroken()) {
			logger.warn("Asset is broken, skipping dispose for " + gameAsset.getRootRawAsset().handle.path());
		}

		try {
			switch (assetTypeFromExtension) {
			case SPRITE:

				GameAsset<AtlasSprite> textureGameAsset = (GameAsset<AtlasSprite>)gameAsset;
				textureGameAsset.getResource().getTexture().dispose();
				break;
			case ATLAS:
				GameAsset<TextureAtlas> textureAtlasGameAsset = (GameAsset<TextureAtlas>)gameAsset;
				textureAtlasGameAsset.getResource().dispose();
				break;

			case SKELETON:
				// TODO: 14.02.23 figure out how to dispose skeleton's atlas
				break;
			case SOUND:

				GameAsset<Music> musicGameAsset = (GameAsset<Music>)gameAsset;
				musicGameAsset.getResource().dispose();
				break;
			case VFX_OUTPUT:
				break;
			case VFX:
				break;
			case SCRIPT:
				break;
			case ROUTINE:
				break;
			case PREFAB:
				break;
			case SCENE:
				break;
			case TILE_PALETTE:
				break;
			case LAYOUT_DATA:
				break;
			case DIRECTORY:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (gameAsset != null) {
				logger.error("Asset " + gameAsset.getRootRawAsset().handle.path() + " could not be disposed!");
			}
		}
	}

	public void saveMetaData (AMetadata metaData, boolean useGlobalState) {
		if (useGlobalState) {
			GlobalSaveStateSystem.MetaDataUpdateStateObject metaDataUpdateStateObject = new GlobalSaveStateSystem.MetaDataUpdateStateObject(metaData);
			SharedResources.globalSaveStateSystem.pushItem(metaDataUpdateStateObject);
		}
		saveMetaDataToFile(metaData);
	}

	public void saveMetaDataToFile (AMetadata metadata) {
		RawAsset link = metadata.link;
		FileHandle metadataHandleFor = AssetImporter.getMetadataHandleFor(link.handle);
		metadataHandleFor.writeString(json.prettyPrint(metadata), false);
	}

	public void assetChanged (GameAsset<?> gameAsset) {
		Toasts.getInstance().showInfoToast("Marked changes " + gameAsset.nameIdentifier);

		GlobalSaveStateSystem.GameAssetUpdateStateObject gameAssetUpdateStateObject = new GlobalSaveStateSystem.GameAssetUpdateStateObject(gameAsset);
		SharedResources.globalSaveStateSystem.pushItem(gameAssetUpdateStateObject);
	}

	public void saveGameAssetResourceJsonToFile (GameAsset<?> gameAsset, boolean useGlobalState) {
		if (useGlobalState) {
			GlobalSaveStateSystem.GameAssetUpdateStateObject gameAssetUpdateStateObject = new GlobalSaveStateSystem.GameAssetUpdateStateObject(gameAsset);
			SharedResources.globalSaveStateSystem.pushItem(gameAssetUpdateStateObject);
		}

		saveGameAssetResourceJsonToFile(gameAsset);
	}

	private interface GameResourceSaveStrategy<T> {
		String serializeToJson (GameAsset<T> gameAsset, Json json);
	}

	private ObjectMap<GameAssetType, GameResourceSaveStrategy> saveStrategyObjectMap = new ObjectMap<>();

	{
		saveStrategyObjectMap.put(GameAssetType.SCENE, this::serializeScene);
		saveStrategyObjectMap.put(GameAssetType.PREFAB, this::serializePrefab);
		saveStrategyObjectMap.put(GameAssetType.ROUTINE, this::serializeRoutine);
		saveStrategyObjectMap.put(GameAssetType.VFX, this::serializeVFX);
	}

	private String serializeRoutine (GameAsset<RoutineStageData> gameAsset, Json json) {
		RoutineStageData resource = gameAsset.getResource();
		return json.prettyPrint(resource);
	}

	private String serializeVFX (GameAsset<VFXProjectData> gameAsset, Json json) {
		VFXProjectData resource = gameAsset.getResource();
		return json.prettyPrint(resource);
	}

	private String serializeScene (GameAsset<Scene> gameAsset, Json json) {

		Scene resource = gameAsset.getResource();
		String asString = resource.getAsString();

		return asString;
	}

	private String serializePrefab (GameAsset<Prefab> gameAsset, Json json) {

		Prefab resource = gameAsset.getResource();
		String asString = resource.getAsString();

		return asString;
	}

	public <T> String saveGameAssetCurrentStateToJsonString (GameAsset<T> gameAsset) {
		GameResourceSaveStrategy<T> gameResourceSaveStrategy = saveStrategyObjectMap.get(gameAsset.type);
		if (gameResourceSaveStrategy != null) {

			String jsonString = gameResourceSaveStrategy.serializeToJson(gameAsset, json);

			if (jsonString == null) {
				Toasts.getInstance().showErrorToast("Error saving asset " + gameAsset);
				logger.error("Error saving asset");
				return null;
			}

			return jsonString;

		} else {
			Toasts.getInstance().showErrorToast("Trying to save an asset that doesn't have a save strategy " + gameAsset);
			logger.error("Trying to save an asset that doesn't have a save strategy");
		}

		return null;
	}

	public <T> void saveGameAssetResourceJsonToFile (GameAsset<T> gameAsset) {
		Toasts.getInstance().showInfoToast("Saved to file " + gameAsset.nameIdentifier);
		RawAsset rootRawAsset = gameAsset.getRootRawAsset();

		String jsonString = saveGameAssetCurrentStateToJsonString(gameAsset);

		if (jsonString == null) {
			logger.error("Error saving to file");
			return;
		}

		rootRawAsset.handle.writeString(jsonString, false);
		SharedResources.globalSaveStateSystem.markSaved(gameAsset);

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
			} else if (!dataMaps.fileHandleRawAssetMap.containsKey(fileHandle)) {
				rawAssetCreated(fileHandle, checkGameResources);
			}
		}
	}

	private boolean shouldIgnoreAsset (FileHandle fileHandle) {
		String extension = fileHandle.extension();

		if (fileHandle.name().equals(".DS_Store"))
			return true;
		if (extension.equals("meta"))
			return true;

		return false;
	}

	public void registerFileWatching () {

	}

	public void createPForTLSIfNotExist (RawAsset tlsRawAsset, boolean checkGameResources) {
		if (checkGameResources) {
			VFXProjectData projectData = VFXProjectSerializer.readTalosTLSProject(tlsRawAsset.handle);
			ExportData exportData = VFXProjectSerializer.exportTLSDataToP(projectData);
			String exportDataJson = VFXProjectSerializer.writeTalosPExport(exportData);

			FileHandle exportedPFile = tlsRawAsset.handle.parent().child(tlsRawAsset.handle.nameWithoutExtension() + ".p");
			exportedPFile.writeString(exportDataJson, false);

			rawAssetCreated(exportedPFile, checkGameResources);
		}
	}

	public void rawAssetCreated (FileHandle fileHandle, boolean checkGameResources) {
		newFilesSeen.add(fileHandle);
		try {
			GameAssetType assetTypeFromExtension = GameAssetType.getAssetTypeFromExtension(fileHandle.extension());

			RawAsset rawAsset = new RawAsset(fileHandle);

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

			dataMaps.putUUIDRawAsset(rawAsset.metaData.uuid, rawAsset);
			dataMaps.putFileHandleRawAsset(fileHandle, rawAsset);

			if (checkGameResources) {
				checkAllGameAssetCreation();
			}
		} catch (GameAssetType.NoAssetTypeException noAssetTypeException) {
			//Its not an asset
		}
	}

	@EventHandler
	public void onProjectLoad (ProjectLoadedEvent projectLoadedEvent) {
		loadAssetsForProject(projectLoadedEvent.getProjectData().rootProjectDir());
	}

	@EventHandler
	public void onProjectUnload (ProjectUnloadEvent projectUnloadEvent) {
		unloadAssets();
	}

	@EventHandler
	public void onScriptFileChanged (ScriptFileChangedEvent event) {
		FileHandle realScriptHandle = event.file;
		String proxyMetaPath = realScriptHandle.path() + ".meta";
		FileHandle proxyMetaFileHandle = new FileHandle(proxyMetaPath);
		if (!proxyMetaFileHandle.exists()) {
			return;
		}

		Class<? extends AMetadata> metaClassForType = GameAssetType.getMetaClassForType(GameAssetType.SCRIPT);
		ScriptMetadata metadata = (ScriptMetadata)json.fromJson(metaClassForType, proxyMetaFileHandle);

		RawAsset rawAsset = dataMaps.uuidRawAssetMap.get(metadata.uuid);
		FileHandle proxyScriptHandle = dataMaps.fileHandleRawAssetMap.findKey(rawAsset, true);
		if (rawAsset == null) {
			// CASE: someone put file handle externally in project folder
			return;
		}
		realScriptHandle.copyTo(proxyScriptHandle);

		AMetadata metaData = rawAsset.metaData;
		if (metaData instanceof ScriptMetadata) {
			metaData.postProcessForHandle(realScriptHandle);

			//Save the meta data
			FileHandle metadataHandleFor = AssetImporter.getMetadataHandleFor(proxyScriptHandle);
			metadataHandleFor.writeString(json.prettyPrint(metaData), false);

			logger.info("redo script meta changing");

//			GameObject rootGO = SceneEditorWorkspace.getInstance().getRootGO();
//			Array<ScriptComponent> updatedComponents = new Array<>();
//			updateScriptComponentsForNewMeta(rootGO, (ScriptMetadata) metaData, updatedComponents);

//			Gdx.app.postRunnable(new Runnable() {
//				@Override
//				public void run () {
//					for (ScriptComponent updatedComponent : updatedComponents) {
//						Notifications.fireEvent(Notifications.obtainEvent(ComponentUpdated.class).set(updatedComponent, false));
//					}
//				}
//			});
		}
	}

	private void updateScriptComponentsForNewMeta (GameObject gameObject, ScriptMetadata metaData, Array<ScriptComponent> updatedComponents) {
		if (gameObject.hasComponent(ScriptComponent.class)) {
			ScriptComponent component = gameObject.getComponent(ScriptComponent.class);
			AMetadata componentMeta = component.getGameResource().getRootRawAsset().metaData;
			if (componentMeta.uuid == metaData.uuid) {
				component.importScriptPropertiesFromMeta(true);
				updatedComponents.add(component);
			}
		}

		Array<GameObject> children = gameObject.getGameObjects();
		if (children != null) {
			for (int i = 0; i < children.size; i++) {
				GameObject child = children.get(i);
				updateScriptComponentsForNewMeta(child, metaData, updatedComponents);
			}
		}
	}

	private <T extends AMetadata> T createMetaDataForAsset (RawAsset rawAsset) {
		if (rawAsset.handle.isDirectory()) {
			return (T)new DirectoryMetadata();
		} else {

			FileHandle rawAssetHandle = rawAsset.handle;
			String extension = rawAssetHandle.extension();

			GameAssetType assetTypeFromExtension = null;
			try {
				assetTypeFromExtension = GameAssetType.getAssetTypeFromExtension(extension);
			} catch (GameAssetType.NoAssetTypeException e) {
				throw new RuntimeException(e);
			}
			T metaForType = (T)GameAssetType.createMetaForType(assetTypeFromExtension);

			if (metaForType instanceof ScriptMetadata) {
				metaForType.postProcessForHandle(rawAssetHandle);
			}

			//Save the meta data
			FileHandle metadataHandleFor = AssetImporter.getMetadataHandleFor(rawAssetHandle);
			metadataHandleFor.writeString(json.prettyPrint(metaForType), false);

			metaForType.setLinkRawAsset(rawAsset);

			return metaForType;
		}
	}

	public static FileHandle getExportedScriptsFolderHandle () {
		logger.info("Redo scripts folder to not be unique");
//		String projectPath = SceneEditorWorkspace.getInstance().getProjectPath();
//		return Gdx.files.absolute(projectPath).parent().child("src").child("scene").child("scripts");
		return Gdx.files.local(".");
	}

	public GameAsset<?> getAssetForPath (FileHandle file, boolean ignoreBroken) {
		if (dataMaps.fileHandleGameAssetObjectMap.containsKey(file)) {
			GameAsset<?> gameAsset = dataMaps.fileHandleGameAssetObjectMap.get(file);
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

	public <T> Array<GameAsset<T>> getAssetsForType (GameAssetType type) {
		Array<GameAsset<T>> assets = new Array<>();
		for (GameAsset value : dataMaps.fileHandleGameAssetObjectMap.values()) {
			if (value.type == type) {
				assets.add(value);
			}
		}
		return assets;
	}

	private void deleteFileImpl (FileHandle handle) {
		FileHandle metadataHandleFor = AssetImporter.getMetadataHandleFor(handle);
		if (metadataHandleFor.exists()) {
			metadataHandleFor.delete();
		}
		handle.delete();
		Array<GameAsset> gameAssetsToUpdate = new Array<>();

		if (dataMaps.fileHandleRawAssetMap.containsKey(handle)) {
			RawAsset rawAsset = dataMaps.fileHandleRawAssetMap.get(handle);
			gameAssetsToUpdate.addAll(rawAsset.gameAssetReferences);

			for (GameAsset gameAsset : gameAssetsToUpdate) {
				if (gameAsset.dependentRawAssets.size == 1) {
					SharedResources.appManager.onAssetDeleted(gameAsset);
				}

				gameAsset.dependentRawAssets.removeValue(rawAsset, true);

				//check if this is a broken game asset, or one to be removed
				if (gameAsset.dependentRawAssets.size > 0) {
					gameAsset.setBroken(new Exception("Removed one of the dependent raw files"));
					gameAsset.setUpdated();
				} else {
					gameAsset.setBroken(new Exception("Game Asset Removed"));
					dataMaps.removeFileHandleGameAssetObjectMap(rawAsset.handle);
					identifierGameAssetMap.get(gameAsset.type).remove(gameAsset.nameIdentifier);
					uniqueIdentifierGameAssetMap.get(gameAsset.type).remove(rawAsset.metaData.uuid);
				}

			}

			dataMaps.removeFileHandleRawAsset(handle);
			dataMaps.removeUUIDRawAsset(rawAsset.metaData.uuid);
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

	public FileHandle copyRawAsset (FileHandle file, FileHandle directory) {
		return copyRawAsset(file, directory, false);
	}

	/**
	 * In the first synopsis form, the copyRawAsset utility copies the contents of the file to the directory.
	 * In the second synopsis form, the contents of each named file is copied to the directory target_directory.
	 * The names of the files themselves are changed in case of collision, while replace flag is false.
	 *
	 * @param file
	 * @param directory
	 * @param replace
	 * @return FileHandle of newly copied file.
	 */
	public FileHandle copyRawAsset (FileHandle file, FileHandle directory, boolean replace) {
		// TODO: 11.01.23 rename arguments
		String fileName = directory.isDirectory() ? file.name() : directory.name();
		final FileHandle destinationDirectory = directory.isDirectory() ? directory : directory.parent();

		if (destinationDirectory.child(fileName).exists() && !replace) {
			String baseName = directory.isDirectory() ? file.nameWithoutExtension() : directory.nameWithoutExtension();

			fileName = NamingUtils.getNewName(baseName, new Supplier<Collection<String>>() {
				@Override
				public Collection<String> get () {
					ArrayList<String> fileNames = new ArrayList<>();
					for (FileHandle fileHandle : destinationDirectory.list()) {
						fileNames.add(fileHandle.nameWithoutExtension());
					}
					return fileNames;
				}
			}) + "." + file.extension();

		}
		// do not allow stupid characters
		Pattern pattern = Pattern.compile("[/?<>\\\\:*|\"]");
		Matcher matcher = pattern.matcher(fileName);
		fileName = matcher.replaceAll("_");
		FileHandle dest = destinationDirectory.child(fileName);
		if (file.isDirectory()) { // recursively copy directory and its contents
			FileHandle[] list = file.list();
			//Change the destination and copy all its children into the new destination
			dest.mkdirs();
			for (FileHandle fileHandle : list) {
				if (fileHandle.extension().equals("meta"))
					continue; //Don't copy meta

				copyRawAsset(fileHandle, dest);
			}
		} else { // single file
			file.copyTo(dest);
		}

		collectRawResourceFromDirectory(dest, true);

		return dest;
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
				RawAsset rawAsset = dataMaps.removeFileHandleRawAsset(oldHandle);

				if (rawAsset == null) {
					System.out.println();
				}
				rawAsset.handle = newHandle;

				dataMaps.putFileHandleRawAsset(newHandle, rawAsset);

				for (GameAsset gameAssetReference : rawAsset.gameAssetReferences) {
					gameAssetReference.setUpdated();
				}

				if (isRootGameResource(rawAsset)) {
					GameAsset gameAsset = dataMaps.removeFileHandleGameAssetObjectMap(oldHandle);
					dataMaps.putFileHandleGameAsset(newHandle, gameAsset);

					gameAsset.setUpdated();
				}

				AssetPathChanged assetPathChanged = Notifications.obtainEvent(AssetPathChanged.class);
				assetPathChanged.oldHandle = oldHandle;
				assetPathChanged.newHandle = newHandle;
				Notifications.fireEvent(assetPathChanged);
			}
			updateChildReferences(child);

		}
	}

	private void populateChildren (FileHandle fileHandle, MovingDirNode fileNode) {
		FileHandle[] children = fileHandle.list();
		for (FileHandle handle : children) {
			if (handle.extension().equals("meta"))
				continue;

			MovingDirNode value = new MovingDirNode();

			value.oldHandle = handle;
			fileNode.children.add(value);

			if (handle.isDirectory()) {
				populateChildren(handle, value);
			}
		}
	}

	//Could be a rename or a move
	public void moveFile (FileHandle file, FileHandle destination, boolean rename) {
		AssetImporter.moveFile(file, destination, true, rename);
	}

	public void moveFile (FileHandle file, FileHandle destination, boolean checkGameAssets, boolean rename) {

		if (file.isDirectory()) {
			//Moving a folder
			if (destination.exists() && !destination.isDirectory()) {
				throw new GdxRuntimeException("Trying to move a directory to a file");
			}

			if (!rename && !destination.name().equals(file.name())) {
				//Make sure to preserve the file structure when copying folders
				destination = destination.child(file.name());
			}

			MovingDirNode rootNode = new MovingDirNode();
			rootNode.oldHandle = file;
			rootNode.newHandle = destination;
			populateChildren(file, rootNode);

			file.moveTo(destination);
			Notifications.fireEvent(Notifications.obtainEvent(DirectoryMovedEvent.class).set(file, destination));

			updateChildReferences(rootNode);

		} else {
			//Moving a file
			if (destination.isDirectory()) {
				//Moving a file into a directory

				RawAsset rawAsset = dataMaps.removeFileHandleRawAsset(file);
				dataMaps.removeFileHandleGameAssetObjectMap(file);

				FileHandle oldMeta = AssetImporter.getMetadataHandleFor(file);

				if (oldMeta.exists()) {
					oldMeta.moveTo(destination);
				}

				file.moveTo(destination);

				if (rawAsset == null) {
					//It wasn't something we were tracking, must be broken so we just ignore it

					return;
				}

				//Lets check to see if its a tls for special case
				if (file.extension().equals("tls")) {
					//We need to move the .p too
					FileHandle pFile = file.parent().child(file.nameWithoutExtension() + ".p");
					if (pFile.exists()) {
						//Copy this too,
						AssetImporter.moveFile(pFile, destination, false, false);
					}
				}

				FileHandle newHandle = destination.child(file.name());

				dataMaps.putFileHandleRawAsset(newHandle, rawAsset);

				rawAsset.handle = newHandle;

				if (isRootGameResource(rawAsset)) {
					UUID getAssetUniqueIdentifierFromRawAsset = getGameAssetUniqueIdentifierFromRawAsset(rawAsset);
					GameAssetType typeFromExtension = null;
					try {
						typeFromExtension = GameAssetType.getAssetTypeFromExtension(rawAsset.handle.extension());
					} catch (GameAssetType.NoAssetTypeException e) {
						throw new RuntimeException(e);
					}

					GameAsset<?> assetForUniqueIdentifier = getAssetForUniqueIdentifier(getAssetUniqueIdentifierFromRawAsset, typeFromExtension);

					if (assetForUniqueIdentifier != null) {
						dataMaps.fileHandleGameAssetObjectMap.remove(file);

						String newAssetName = newHandle.nameWithoutExtension();

						assetForUniqueIdentifier.nameIdentifier = newAssetName;
						dataMaps.fileHandleGameAssetObjectMap.put(newHandle, assetForUniqueIdentifier);

						assetForUniqueIdentifier.setUpdated();
					} else {
						System.err.println("No game asset found for identifier " + rawAsset.handle.path());
					}
				}

				for (GameAsset gameAssetReference : rawAsset.gameAssetReferences) {
					gameAssetReference.setUpdated();
				}

			} else {
				//Its a rename
				if (dataMaps.fileHandleRawAssetMap.containsKey(file)) {
					RawAsset rawAsset = dataMaps.fileHandleRawAssetMap.get(file);
					dataMaps.removeFileHandleRawAsset(file);

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

					if (isRootGameResource(rawAsset)) {
						UUID gameAssetUniqueIdentifierFromRawAsset = getGameAssetUniqueIdentifierFromRawAsset(rawAsset);
						GameAssetType typeFromExtension = null;
						try {
							typeFromExtension = GameAssetType.getAssetTypeFromExtension(rawAsset.handle.extension());
						} catch (GameAssetType.NoAssetTypeException e) {
							throw new RuntimeException(e);
						}

						GameAsset<?> assetForUniqueIdentifier = getAssetForUniqueIdentifier(gameAssetUniqueIdentifierFromRawAsset, typeFromExtension);

						if (assetForUniqueIdentifier != null) {
							dataMaps.fileHandleGameAssetObjectMap.remove(file);

							String newAssetName = destination.nameWithoutExtension();
							assetForUniqueIdentifier.nameIdentifier = newAssetName;
							dataMaps.fileHandleGameAssetObjectMap.put(destination, assetForUniqueIdentifier);

							assetForUniqueIdentifier.setUpdated();
						} else {
							System.err.println("No game asset found for identifier " + rawAsset.handle.path());
						}
					}

					dataMaps.putFileHandleRawAsset(destination, rawAsset);

					rawAsset.handle = destination;

					if (isRootGameResource(rawAsset)) {
						handleRootGameResourceRename(rawAsset, destination);
					}

					for (GameAsset gameAssetReference : rawAsset.gameAssetReferences) {
						gameAssetReference.setUpdated();
					}

					AssetPathChanged assetPathChanged = Notifications.obtainEvent(AssetPathChanged.class);
					assetPathChanged.oldHandle = file;
					assetPathChanged.newHandle = destination;
					Notifications.fireEvent(assetPathChanged);

				} else {
					//Just move it
					file.moveTo(destination);

					collectRawResourceFromDirectory(file, true);
				}
			}

		}

		if (checkGameAssets) {
			checkAllGameAssetCreation();
		}
	}

	private void handleRootGameResourceRename (RawAsset rawAsset, FileHandle newHandle) {
		UUID gameAssetUniqueIdentifierFromRawAsset = getGameAssetUniqueIdentifierFromRawAsset(rawAsset);
		GameAssetType typeFromExtension = null;
		try {
			typeFromExtension = GameAssetType.getAssetTypeFromExtension(rawAsset.handle.extension());
		} catch (GameAssetType.NoAssetTypeException e) {
			throw new RuntimeException(e);
		}

		GameAsset<?> assetForUniqueIdentifier = getAssetForUniqueIdentifier(gameAssetUniqueIdentifierFromRawAsset, typeFromExtension);

		if (assetForUniqueIdentifier != null) {
			if (typeFromExtension == GameAssetType.SCENE) {
				GameAsset<Scene> sceneAsset = (GameAsset<Scene>)assetForUniqueIdentifier;
				sceneAsset.getResource().setName(newHandle.nameWithoutExtension());
				saveGameAssetResourceJsonToFile(assetForUniqueIdentifier);
			} else if (typeFromExtension == GameAssetType.PREFAB) {
				GameAsset<Prefab> prefabAsset = (GameAsset<Prefab>)assetForUniqueIdentifier;
				prefabAsset.getResource().setName(newHandle.nameWithoutExtension());
				saveGameAssetResourceJsonToFile(assetForUniqueIdentifier);
			}
		} else {
			System.err.println("No game asset found for identifier " + rawAsset.handle.path());
		}
	}

	public void resizeAsset (GameAsset<AtlasSprite> gameAsset, int width, int height) {
		final FileHandle fileHandle = gameAsset.getRootRawAsset().handle;
		final Pixmap oldPixmap = new Pixmap(fileHandle);
		final Pixmap newPixmap = new Pixmap(width, height, oldPixmap.getFormat());
		newPixmap.drawPixmap(oldPixmap, 0, 0);

		PixmapIO.writePNG(fileHandle, newPixmap);

		gameAsset.setResourcePayload(new AtlasSprite(new TextureAtlas.AtlasRegion(new TextureRegion(new Texture(newPixmap)))));
		gameAsset.setUpdated();

		if (!oldPixmap.isDisposed()) {
			oldPixmap.dispose();
		}

		// fire asset resolution changed event
		final AssetResolutionChanged event = Notifications.obtainEvent(AssetResolutionChanged.class);
		event.setFileHandle(fileHandle);
		Notifications.fireEvent(event);
	}

	public void fillAssetColor (GameAsset<AtlasSprite> gameAsset, Color color) {
		final FileHandle fileHandle = gameAsset.getRootRawAsset().handle;
		final Pixmap pixmap = new Pixmap(fileHandle);

		pixmap.setColor(color);
		pixmap.fill();

		PixmapIO.writePNG(fileHandle, pixmap);

		if (!gameAsset.isBroken()) {
			AtlasSprite resource = gameAsset.getResource();
			TextureData textureData = resource.getTexture().getTextureData();
			if (textureData instanceof PixmapTextureData) {
				textureData.consumePixmap().dispose();
			}
			resource.getTexture().dispose();
		}

		gameAsset.setResourcePayload(new AtlasSprite(new TextureAtlas.AtlasRegion(new TextureRegion(new Texture(pixmap)))));
		gameAsset.setUpdated();

		// fire asset color fill event
		final AssetColorFillEvent event = Notifications.obtainEvent(AssetColorFillEvent.class);
		event.setFileHandle(fileHandle);
		Notifications.fireEvent(event);
	}

	public static String relative (String fullPath) {
		return relative(Gdx.files.absolute(fullPath));
	}

	public static String relative (FileHandle fileHandle) {
		logger.info("should be removed, not sure why we need this");
		String projectPath = SharedResources.currentProject.rootProjectDir().path();

		String path = fileHandle.path();
		if (path.startsWith(projectPath)) {
			path = path.substring(projectPath.length());
		}

		return path;
	}

	private boolean isRootGameResource (RawAsset rawAsset) {
		GameAssetType assetTypeFromExtension = null;
		try {
			assetTypeFromExtension = GameAssetType.getAssetTypeFromExtension(rawAsset.handle.extension());
		} catch (GameAssetType.NoAssetTypeException e) {
			throw new RuntimeException(e);
		}
		return assetTypeFromExtension.isRootGameAsset();
	}

	private static class AssetNameFieldFilter implements TextField.TextFieldFilter {

		@Override
		public boolean acceptChar (TextField textField, char c) {
			return !(c == '/' || c == '?' || c == '<' || c == '>' || c == '\\' || c == ':' || c == '*' || c == '|' || c == '"');
		}
	}
}
