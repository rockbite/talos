package com.talosvfx.talos.runtime.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.spine.SkeletonBinary;
import com.esotericsoftware.spine.SkeletonData;
import com.talosvfx.talos.runtime.assets.meta.AtlasMetadata;
import com.talosvfx.talos.runtime.assets.meta.EmptyMetadata;
import com.talosvfx.talos.runtime.assets.meta.PrefabMetadata;
import com.talosvfx.talos.runtime.assets.meta.SceneMetadata;
import com.talosvfx.talos.runtime.assets.meta.SpineMetadata;
import com.talosvfx.talos.runtime.assets.meta.SpriteMetadata;
import com.talosvfx.talos.runtime.routine.serialization.BaseRoutineData;
import com.talosvfx.talos.runtime.routine.serialization.RuntimeRoutineData;
import com.talosvfx.talos.runtime.scene.Prefab;
import com.talosvfx.talos.runtime.scene.Scene;
import com.talosvfx.talos.runtime.vfx.ParticleEffectDescriptor;
import com.talosvfx.talos.runtime.vfx.assets.AssetProvider;
import com.talosvfx.talos.runtime.vfx.serialization.BaseVFXProjectData;
import com.talosvfx.talos.runtime.vfx.serialization.ExportData;

import java.util.UUID;

public class RuntimeAssetRepository extends BaseAssetRepository implements AssetProvider {
	protected ObjectMap<GameAssetType, ObjectMap<String, GameAsset<?>>> identifierToGameAssetMap = new ObjectMap<>();
	protected ObjectMap<UUID, GameAsset<?>> uuidGameAssetObjectMap = new ObjectMap<>();

	protected ObjectMap<GameAsset<Texture>, NinePatch> patchCache = new ObjectMap<>();

	public RuntimeAssetRepository () {
	}

	public void loadBundle (GameAssetsExportStructure gameAssetFile, FileHandle baseDir) {

		ObjectMap<GameAssetType, Array<GameAssetExportStructure>> sorted = new ObjectMap<>();
		for (GameAssetExportStructure gameAsset : gameAssetFile.gameAssets) {
			if (!sorted.containsKey(gameAsset.type)) {
				sorted.put(gameAsset.type, new Array<>());
			}
			sorted.get(gameAsset.type).add(gameAsset);
		}

		loadType(GameAssetType.SPRITE, sorted, baseDir);
		loadType(GameAssetType.SCRIPT, sorted, baseDir);
		loadType(GameAssetType.ATLAS, sorted, baseDir);
		loadType(GameAssetType.SOUND, sorted, baseDir);

		loadType(GameAssetType.SKELETON, sorted, baseDir);

		loadType(GameAssetType.VFX, sorted, baseDir);
		loadType(GameAssetType.VFX_OUTPUT, sorted, baseDir);
		loadType(GameAssetType.ROUTINE, sorted, baseDir);
		loadType(GameAssetType.PREFAB, sorted, baseDir);
		loadType(GameAssetType.SCENE, sorted, baseDir);

		loadType(GameAssetType.TILE_PALETTE, sorted, baseDir);

		loadType(GameAssetType.LAYOUT_DATA, sorted, baseDir);
	}

	@Override
	public <T> T findAsset (String assetName, Class<T> clazz) {
		if (Sprite.class.isAssignableFrom(clazz)) {
			GameAsset<Texture> resource = getAssetForIdentifier(assetName, GameAssetType.SPRITE);
			return (T)new Sprite(resource.getResource());
		}
		throw new GdxRuntimeException("No asset found " + assetName + " " + clazz.getSimpleName());
	}

	@Override
	public <T> GameAsset findGameAsset(String assetName, Class<T> clazz) {
		throw new UnsupportedOperationException("Not supported operation");
	}

	private interface GameAssetLoader<T> {
		GameAsset<T> load (GameAssetExportStructure exportStructure, FileHandle baseFolder);
	}

	private GameAssetLoader<?> getLoader (GameAssetType type) {
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
		GameAsset<Texture> gameAsset = new GameAsset<>(exportStructure.identifier, exportStructure.type);

		String first = exportStructure.relativePathsOfRawFiles.first();
		FileHandle child = baseFolder.child(exportStructure.type.name()).child(first);

		gameAsset.setResourcePayload(new Texture(child));
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
		String skeleName = skeleFile.nameWithoutExtension();
		FileHandle atlasFile = skeleFile.parent().child(skeleName + ".atlas");
		TextureAtlas.TextureAtlasData skeleAtlasData = new TextureAtlas.TextureAtlasData(atlasFile, atlasFile.parent(), false);
		TextureAtlas skeleAtlas = new TextureAtlas(skeleAtlasData);

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
		particleEffectDescriptor.setAssetProvider(this);

		particleEffectDescriptor.load(vfxExportData);

		vfxExportData.setDescriptorLoaded(particleEffectDescriptor);

		gameAsset.setResourcePayload(vfxExportData);

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
		scene.path = sceneFile.path();
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
	public NinePatch obtainNinePatch (GameAsset<Texture> gameAsset) {
		if (patchCache.containsKey(gameAsset)) { //something better, maybe hash on pixel size + texture for this
			return patchCache.get(gameAsset);
		} else {
			final SpriteMetadata metadata = (SpriteMetadata)gameAsset.getRootRawAsset().metaData;
			final NinePatch patch = new NinePatch(gameAsset.getResource(), metadata.borderData[0], metadata.borderData[1], metadata.borderData[2], metadata.borderData[3]);
			patch.scale(1 / metadata.pixelsPerUnit, 1 / metadata.pixelsPerUnit); // fix this later
			patchCache.put(gameAsset, patch);
			return patch;
		}
	}
}
