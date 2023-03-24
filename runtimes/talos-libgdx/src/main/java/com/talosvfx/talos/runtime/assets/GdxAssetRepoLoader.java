package com.talosvfx.talos.runtime.assets;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.talosvfx.talos.runtime.RuntimeContext;

import static com.talosvfx.talos.runtime.assets.GameAssetLoader.getAssetDescriptorForGameAsset;

public class GdxAssetRepoLoader extends AsynchronousAssetLoader<GdxAssetRepo, GdxAssetRepoLoader.GdxAssetRepoLoaderParam> {

	private GameAssetsExportStructure gameAssetsExportStructure;
	private Array<AssetDescriptor> dependencies;
	private GdxAssetRepo assetRepo;
	private FileHandle baseFolder;

	public GdxAssetRepoLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, FileHandle file, GdxAssetRepoLoaderParam parameter) {
		dependencies = new Array<>();

		assetRepo = new GdxAssetRepo();
		RuntimeContext instance = RuntimeContext.getInstance();
		instance.setAssetRepository(assetRepo);

		baseFolder = parameter.exportFile.parent();

		gameAssetsExportStructure = new Json().fromJson(GameAssetsExportStructure.class, parameter.exportFile);

		assetRepo.setSceneData(gameAssetsExportStructure.sceneData);
		instance.setSceneData(gameAssetsExportStructure.sceneData);

		for (GameAssetExportStructure gameAsset : gameAssetsExportStructure.gameAssets) {
			AssetDescriptor<GameAsset> assetDescriptorForGameAsset = getAssetDescriptorForGameAsset(gameAssetsExportStructure, gameAsset, assetRepo, baseFolder);
			dependencies.add(assetDescriptorForGameAsset);
		}

		return dependencies;
	}

	@Override
	public void loadAsync (AssetManager manager, String fileName, FileHandle file, GdxAssetRepoLoaderParam parameter) {
		if (parameter.exportFile == null) throw new GdxRuntimeException("Must provide valid file in loader param");

		//Anything non
	}

	@Override
	public GdxAssetRepo loadSync (AssetManager manager, String fileName, FileHandle file, GdxAssetRepoLoaderParam parameter) {

		//Any opengl parts


		GdxAssetRepo repo = assetRepo;

		gameAssetsExportStructure = null;
		assetRepo = null;
		dependencies = null;
		baseFolder = null;

		return repo;
	}

	public static class GdxAssetRepoLoaderParam extends AssetLoaderParameters<GdxAssetRepo> {
		public FileHandle exportFile;
	}
}
