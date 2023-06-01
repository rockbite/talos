package com.talosvfx.talos.runtime.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasSprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.esotericsoftware.spine.SkeletonBinary;
import com.esotericsoftware.spine.SkeletonData;
import com.talosvfx.talos.runtime.RuntimeContext;
import com.talosvfx.talos.runtime.assets.meta.*;
import com.talosvfx.talos.runtime.graphics.NineSlice;
import com.talosvfx.talos.runtime.routine.serialization.BaseRoutineData;
import com.talosvfx.talos.runtime.routine.serialization.RuntimeRoutineData;
import com.talosvfx.talos.runtime.scene.Prefab;
import com.talosvfx.talos.runtime.scene.Scene;
import com.talosvfx.talos.runtime.scene.SceneData;
import com.talosvfx.talos.runtime.vfx.ParticleEffectDescriptor;
import com.talosvfx.talos.runtime.vfx.serialization.BaseVFXProjectData;
import com.talosvfx.talos.runtime.vfx.serialization.ExportData;

import java.util.UUID;

public class RuntimeAssetRepository extends BaseAssetRepository {
	protected ObjectMap<GameAssetType, ObjectMap<String, GameAsset<?>>> identifierToGameAssetMap = new ObjectMap<>();
	protected ObjectMap<UUID, GameAsset<?>> uuidGameAssetObjectMap = new ObjectMap<>();

	protected ObjectMap<GameAsset<AtlasSprite>, NineSlice> patchCache = new ObjectMap<>();

	public RuntimeAssetRepository () {
	}

	public void loadBundle (GameAssetsExportStructure gameAssetFile, FileHandle baseDir) {
		RuntimeContext.getInstance().setSceneData(gameAssetFile.sceneData);

		ObjectMap<GameAssetType, Array<GameAssetExportStructure>> sorted = new ObjectMap<>();
		for (GameAssetExportStructure gameAsset : gameAssetFile.gameAssets) {
			if (!sorted.containsKey(gameAsset.type)) {
				sorted.put(gameAsset.type, new Array<>());
			}
			sorted.get(gameAsset.type).add(gameAsset);
		}

		loadType(GameAssetType.ATLAS, sorted, baseDir);
		loadType(GameAssetType.SPRITE, sorted, baseDir);
		loadType(GameAssetType.SCRIPT, sorted, baseDir);
		loadType(GameAssetType.SOUND, sorted, baseDir);

		loadType(GameAssetType.SKELETON, sorted, baseDir);

		loadType(GameAssetType.VFX, sorted, baseDir);
		loadType(GameAssetType.VFX_OUTPUT, sorted, baseDir);
		loadType(GameAssetType.PREFAB, sorted, baseDir);
		loadType(GameAssetType.SCENE, sorted, baseDir);
		loadType(GameAssetType.ROUTINE, sorted, baseDir);

		loadType(GameAssetType.TILE_PALETTE, sorted, baseDir);

		loadType(GameAssetType.LAYOUT_DATA, sorted, baseDir);
	}

	public interface GameAssetLoader<T> {
		GameAsset<T> load (GameAssetExportStructure exportStructure, FileHandle baseFolder);
	}

	public GameAssetLoader<?> getLoader (GameAssetType type) {
		switch (type) {
		case SPRITE:
			return this::spriteLoader;
		case ATLAS:
			return this::atlasLoader;
		case SKELETON:
			return this::skeletonLoader;
		case SOUND:
			break;
		case VFX:
			return this::particleLoader;
		case VFX_OUTPUT:
			break;
		case SCRIPT:
			return this::scriptLoader;
		case ROUTINE:
			return this::routineLoader;
		case PREFAB:
			return this::prefabLoader;
		case SCENE:
			return this::sceneLoader;
		case DIRECTORY:
			break;
		case TILE_PALETTE:
			break;
		case LAYOUT_DATA:
			break;
		}
		throw new GdxRuntimeException("No loader found for type " + type);
	}


	private <T> GameAsset<T> spriteLoader (GameAssetExportStructure exportStructure, FileHandle baseFolder) {
		if (exportStructure.identifier.equals("missing")) {
			GameAsset<T> missingAsset = new GameAsset<>("missing", GameAssetType.SPRITE);
			missingAsset.setBroken(new Exception("Missing asset"));
			return missingAsset;
		}

		GameAsset<AtlasSprite> gameAsset = new GameAsset<>(exportStructure.identifier, exportStructure.type);

		String first = exportStructure.relativePathsOfRawFiles.first();
		FileHandle child = baseFolder.child(exportStructure.type.name()).child(first);

		//Check our game assets, maybe we are referncing an atlas
		ObjectSet<String> dependentGameAssets = exportStructure.dependentGameAssets;
		if (dependentGameAssets.size > 0) {
			String textureAtlasUUID = dependentGameAssets.first();
			GameAsset<TextureAtlas> assetForUniqueIdentifier = getAssetForUniqueIdentifier(UUID.fromString(textureAtlasUUID), GameAssetType.ATLAS);
			if (assetForUniqueIdentifier != null) {

				TextureAtlas resource = assetForUniqueIdentifier.getResource();
				Sprite sprite = resource.createSprite(exportStructure.identifier);
				if (!(sprite instanceof AtlasSprite)) {
					sprite = new AtlasSprite(new TextureAtlas.AtlasRegion(sprite));
				}
				gameAsset.setResourcePayload((AtlasSprite) sprite);
				gameAsset.dependentGameAssets.add(assetForUniqueIdentifier);
				gameAsset.dependentRawAssets.add(fakeMeta(child, SpriteMetadata.class));

				return (GameAsset<T>)gameAsset;
			}
		}

		gameAsset.setResourcePayload(new AtlasSprite(new TextureAtlas.AtlasRegion(new TextureRegion(new Texture(child)))));
		gameAsset.dependentRawAssets.add(fakeMeta(child, SpriteMetadata.class));
		return (GameAsset<T>)gameAsset;
	}



	private <T> GameAsset<T> atlasLoader (GameAssetExportStructure exportStructure, FileHandle baseFolder) {
		GameAsset<TextureAtlas> gameAsset = new GameAsset<>(exportStructure.identifier, exportStructure.type);

		String first = exportStructure.relativePathsOfRawFiles.first();
		FileHandle child = baseFolder.child(exportStructure.type.name()).child(first);

		gameAsset.setResourcePayload(new TextureAtlas(child));
		gameAsset.dependentRawAssets.add(fakeMeta(child, AtlasMetadata.class));
		return (GameAsset<T>)gameAsset;
	}

	private <T> GameAsset<T> skeletonLoader (GameAssetExportStructure exportStructure, FileHandle baseFolder) {
		GameAsset<SkeletonData> gameAsset = new GameAsset<>(exportStructure.identifier, exportStructure.type);

		FileHandle skeleFile = baseFolder.child(exportStructure.type.name()).child(exportStructure.relativePathsOfRawFiles.first());

		//Gotta try load the atlas

		TextureAtlas skeleAtlas = null;
		if (exportStructure.dependentGameAssets.size > 0) {
			String atlasUUID = exportStructure.dependentGameAssets.first();
			GameAsset<TextureAtlas> assetForUniqueIdentifier = getAssetForUniqueIdentifier(UUID.fromString(atlasUUID), GameAssetType.ATLAS);
			if (assetForUniqueIdentifier != null) {
				TextureAtlas resource = assetForUniqueIdentifier.getResource();
				skeleAtlas = resource;
				gameAsset.dependentGameAssets.add(assetForUniqueIdentifier);
			}
		}

		if (skeleAtlas == null) {
			String skeleName = skeleFile.nameWithoutExtension();
			FileHandle atlasFile = skeleFile.parent().child(skeleName + ".atlas");
			TextureAtlas.TextureAtlasData skeleAtlasData = new TextureAtlas.TextureAtlasData(atlasFile, atlasFile.parent(), false);
			skeleAtlas = new TextureAtlas(skeleAtlasData);
		}


		SkeletonBinary skeletonBinary = new SkeletonBinary(skeleAtlas);
		SpineMetadata metaData = getMeta(skeleFile, SpineMetadata.class);
		skeletonBinary.setScale(1f / metaData.pixelsPerUnit);

		SkeletonData skeletonData = skeletonBinary.readSkeletonData(skeleFile);
		gameAsset.setResourcePayload(skeletonData);

		gameAsset.dependentRawAssets.add(fakeMeta(skeleFile, SpineMetadata.class));

		return (GameAsset<T>)gameAsset;
	}
	private <T> GameAsset<T> scriptLoader (GameAssetExportStructure exportStructure, FileHandle baseFolder) {
		GameAsset<String> gameAsset = new GameAsset<>(exportStructure.identifier, exportStructure.type);
		gameAsset.setResourcePayload("Script");
		return (GameAsset<T>)gameAsset;
	}

	private <T> GameAsset<T> particleLoader (GameAssetExportStructure exportStructure, FileHandle baseFolder) {
		GameAsset<BaseVFXProjectData> gameAsset = new GameAsset<>(exportStructure.identifier, exportStructure.type);

		FileHandle vfxPFile = baseFolder.child(exportStructure.type.name()).child(exportStructure.relativePathsOfRawFiles.first());

		ExportData vfxExportData = ParticleEffectDescriptor.getExportData(vfxPFile);

		ParticleEffectDescriptor particleEffectDescriptor = new ParticleEffectDescriptor();

		particleEffectDescriptor.load(vfxExportData);

		vfxExportData.setDescriptorLoaded(particleEffectDescriptor);

		gameAsset.setResourcePayload(vfxExportData);
		gameAsset.dependentRawAssets.add(fakeMeta(vfxPFile, TlsMetadata.class));

		return (GameAsset<T>)gameAsset;
	}


	private <T> GameAsset<T> routineLoader (GameAssetExportStructure exportStructure, FileHandle baseFolder) {
		GameAsset<BaseRoutineData> gameAsset = new GameAsset<>(exportStructure.identifier, exportStructure.type);

		FileHandle routineFile = baseFolder.child(exportStructure.type.name()).child(exportStructure.relativePathsOfRawFiles.first());

		Json json = new Json();
		RuntimeRoutineData runtimeRoutineData = json.fromJson(RuntimeRoutineData.class, routineFile);
		gameAsset.setResourcePayload(runtimeRoutineData);
		gameAsset.dependentRawAssets.add(fakeMeta(routineFile, EmptyMetadata.class));

		return (GameAsset<T>)gameAsset;
	}
	private <T> GameAsset<T> prefabLoader (GameAssetExportStructure exportStructure, FileHandle baseFolder) {
		GameAsset<Prefab> gameAsset = new GameAsset<>(exportStructure.identifier, exportStructure.type);
		FileHandle prefabFile = baseFolder.child(exportStructure.type.name()).child(exportStructure.relativePathsOfRawFiles.first());

		Prefab prefab = new Prefab(prefabFile);

		gameAsset.setResourcePayload(prefab);

		gameAsset.dependentRawAssets.add(fakeMeta(prefabFile, PrefabMetadata.class));

		return (GameAsset<T>)gameAsset;
	}


	private <T> GameAsset<T> sceneLoader (GameAssetExportStructure exportStructure, FileHandle baseFolder) {
		GameAsset<Scene> gameAsset = new GameAsset<>(exportStructure.identifier, exportStructure.type);
		FileHandle sceneFile = baseFolder.child(exportStructure.type.name()).child(exportStructure.relativePathsOfRawFiles.first());

		Scene scene = new Scene();
		scene.loadFromHandle(sceneFile);

		gameAsset.setResourcePayload(scene);
		gameAsset.dependentRawAssets.add(fakeMeta(sceneFile, SceneMetadata.class));

		return (GameAsset<T>)gameAsset;
	}

	private <T extends AMetadata> T getMeta (FileHandle rootFile, Class<T> metaClazz) {
		FileHandle metaFile = rootFile.parent().child(rootFile.name() + ".meta");
		Json json = new Json();
		return json.fromJson(metaClazz, metaFile);
	}

	private <T extends AMetadata> RawAsset fakeMeta (FileHandle child, Class<T> metaClazz) {
		RawAsset fake = new RawAsset(Gdx.files.internal("fake"));
		fake.metaData = getMeta(child, metaClazz) ;
		return fake;
	}


	private void loadType (GameAssetType type, ObjectMap<GameAssetType, Array<GameAssetExportStructure>> sorted, FileHandle baseFolder) {
		Array<GameAssetExportStructure> gameAssetExportStructures = sorted.get(type);

		if (gameAssetExportStructures == null || gameAssetExportStructures.isEmpty()) return;

		GameAssetLoader loader = getLoader(type);
		for (GameAssetExportStructure gameAssetExportStructure : gameAssetExportStructures) {
			GameAsset<?> asset = loader.load(gameAssetExportStructure, baseFolder);
			registerAsset(asset, gameAssetExportStructure.identifier, gameAssetExportStructure.uuid);
		}
	}

	public void registerAsset (GameAsset<?> asset, String uuid) {
		registerAsset(asset, asset.nameIdentifier, uuid);
	}

	private void registerAsset (GameAsset<?> asset, String identifier, String uuid) {
		if (!identifierToGameAssetMap.containsKey(asset.type)) {
			identifierToGameAssetMap.put(asset.type, new ObjectMap<>());
		}
		identifierToGameAssetMap.get(asset.type).put(identifier, asset);
		uuidGameAssetObjectMap.put(UUID.fromString(uuid), asset);
	}

	@Override
	public void reloadGameAssetForRawFile (RawAsset link) {
		throw new GdxRuntimeException("Not implemented for runtime");
	}

	@Override
	public GameAsset<?> getAssetForPath (FileHandle handle, boolean ignoreBroken) {
		throw new GdxRuntimeException("Not implemented for runtime");
	}

	@Override
	public <U> GameAsset<U> getAssetForIdentifier (String identifier, GameAssetType type) {
		ObjectMap<String, GameAsset<?>> entries = identifierToGameAssetMap.get(type);
		if (entries == null || entries.isEmpty()) {
			throw new GdxRuntimeException("No asset found for identifier " + identifier + " " + type);
		}
		return (GameAsset<U>)entries.get(identifier);
	}

	@Override
	public <U> GameAsset<U> getAssetForUniqueIdentifier (UUID uuid, GameAssetType type) {
		return (GameAsset<U>)uuidGameAssetObjectMap.get(uuid);
	}

	@Override
	public NineSlice obtainNinePatch (GameAsset<AtlasSprite> gameAsset) {
		if (patchCache.containsKey(gameAsset)) { //something better, maybe hash on pixel size + texture for this
			return patchCache.get(gameAsset);
		} else {
			final SpriteMetadata metadata = (SpriteMetadata)gameAsset.getRootRawAsset().metaData;
			final NineSlice patch = new NineSlice(gameAsset.getResource(), metadata.borderData[0], metadata.borderData[1], metadata.borderData[2], metadata.borderData[3]);
			patch.scale(1 / metadata.pixelsPerUnit, 1 / metadata.pixelsPerUnit); // fix this later
			patchCache.put(gameAsset, patch);
			return patch;
		}
	}
}
