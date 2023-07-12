package com.talosvfx.talos.runtime.assets;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;

public class GameAssetLoader extends AsynchronousAssetLoader<GameAsset, GameAssetLoader.GameAssetLoaderParam> {

	public GameAssetLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public void loadAsync (AssetManager manager, String fileName, FileHandle file, GameAssetLoaderParam parameter) {

	}

	@Override
	public GameAsset loadSync (AssetManager manager, String fileName, FileHandle file, GameAssetLoaderParam parameter) {
		GdxAssetRepo assetRepo = parameter.assetRepo;

		GameAssetExportStructure gameAsset = parameter.gameAsset;
		RuntimeAssetRepository.GameAssetLoader<?> loader = assetRepo.getLoader(gameAsset.type);

		GameAsset<?> loadedAsset = loader.load(gameAsset, parameter.baseFolder);

		assetRepo.registerAsset(loadedAsset, parameter.gameAsset.uuid);

		return loadedAsset;
	}

	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, GameAssetLoaderParam parameter) {

		Array<AssetDescriptor> dependencies = new Array<>();

		GameAssetExportStructure gameAsset = parameter.gameAsset;
		ObjectSet<String> dependentGameAssets = gameAsset.dependentGameAssets;
		for (String dependentGameAsset : dependentGameAssets) {
			dependencies.add(getAssetDescriptorForGameAsset(parameter.exportStructure, dependentGameAsset, parameter.assetRepo, parameter.baseFolder));
		}

		return dependencies;
	}

	public static AssetDescriptor<GameAsset> getAssetDescriptorForGameAsset (GameAssetsExportStructure gameAssetsExportStructure, String identifier, GameAssetType type, GdxAssetRepo assetRepo, FileHandle baseFolder) {
		GameAssetExportStructure gameAsset = gameAssetsExportStructure.findAsset(identifier, type);
		AssetDescriptor<GameAsset> value = new AssetDescriptor<GameAsset>(gameAsset.uuid, GameAsset.class, new GameAssetLoader.GameAssetLoaderParam(gameAsset, assetRepo, baseFolder, gameAssetsExportStructure));
		return value;
	}
	public static AssetDescriptor<GameAsset> getAssetDescriptorForGameAsset (GameAssetsExportStructure gameAssetsExportStructure, String uuid, GdxAssetRepo assetRepo, FileHandle baseFolder) {
		GameAssetExportStructure gameAsset = gameAssetsExportStructure.findAsset(uuid);
		AssetDescriptor<GameAsset> value = new AssetDescriptor<GameAsset>(uuid, GameAsset.class, new GameAssetLoader.GameAssetLoaderParam(gameAsset, assetRepo, baseFolder, gameAssetsExportStructure));
		return value;
	}
	public static AssetDescriptor<GameAsset> getAssetDescriptorForGameAsset (GameAssetsExportStructure gameAssetsExportStructure, GameAssetExportStructure gameAsset, GdxAssetRepo assetRepo, FileHandle baseFolder) {
		AssetDescriptor<GameAsset> value = new AssetDescriptor<GameAsset>(gameAsset.uuid, GameAsset.class, new GameAssetLoader.GameAssetLoaderParam(gameAsset, assetRepo, baseFolder, gameAssetsExportStructure));
		return value;
	}

	public static class GameAssetLoaderParam extends AssetLoaderParameters<GameAsset> {

		public final GameAssetExportStructure gameAsset;
		public final GdxAssetRepo assetRepo;

		public final FileHandle baseFolder;

		public final GameAssetsExportStructure exportStructure;
		public GameAssetLoaderParam (GameAssetExportStructure gameAsset, GdxAssetRepo assetRepo, FileHandle baseFolder, GameAssetsExportStructure assetsExportStructure) {
			this.gameAsset = gameAsset;
			this.assetRepo = assetRepo;
			this.baseFolder = baseFolder;
			this.exportStructure = assetsExportStructure;
		}
	}

}
