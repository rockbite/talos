
package com.talosvfx.talos.editor.addons.scene.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.spine.SkeletonBinary;
import com.esotericsoftware.spine.SkeletonData;
import com.talosvfx.talos.editor.addons.scene.events.AssetPathChanged;
import com.talosvfx.talos.editor.addons.scene.utils.AMetadata;
import com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter;
import com.talosvfx.talos.editor.addons.scene.utils.metadata.DirectoryMetadata;
import com.talosvfx.talos.editor.notifications.Notifications;

import java.util.UUID;

import static com.talosvfx.talos.editor.addons.scene.utils.importers.AssetImporter.relative;

public class AssetRepository {


	private ObjectMap<String, GameAsset> identifierGameAssetMap = new ObjectMap<>();
	private ObjectMap<FileHandle, GameAsset> fileHandleGameAssetObjectMap = new ObjectMap<>();
	private ObjectMap<UUID, RawAsset> uuidRawAssetMap = new ObjectMap<>();
	private ObjectMap<FileHandle, RawAsset> fileHandleRawAssetMap = new ObjectMap<>();

	private FileHandle assetsRoot;
	private Json json;

	public TextureRegion brokenTextureRegion;


	static AssetRepository instance;
	public static AssetRepository getInstance() {
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

		checkGameAssetCreation();
	}

	private void checkGameAssetCreation () {
		for (ObjectMap.Entry<FileHandle, RawAsset> entry : fileHandleRawAssetMap) {
			FileHandle key = entry.key;
			RawAsset value = entry.value;

			if (key.isDirectory()) continue;

			GameAssetType assetTypeFromExtension = GameAssetType.getAssetTypeFromExtension(key.extension());
			if (assetTypeFromExtension.isRootGameAsset()) {
				createGameAsset(key, value);
			}
		}
	}

	private String getGameAssetIdentifierFromRawAsset (RawAsset asset) {
		return asset.handle.nameWithoutExtension();
	}

	private void createGameAsset (FileHandle key, RawAsset value) {
		String gameAssetIdentifier = getGameAssetIdentifierFromRawAsset(value);

		if (identifierGameAssetMap.containsKey(gameAssetIdentifier) && fileHandleGameAssetObjectMap.containsKey(key)) {
			//Already registered, ignore
			return;
		}

		GameAssetType assetTypeFromExtension = GameAssetType.getAssetTypeFromExtension(key.extension());

		GameAsset gameAsset = createGameAssetForType(assetTypeFromExtension, gameAssetIdentifier, value);

		if (gameAsset == null) return;
		System.out.println("Registering game asset " + gameAssetIdentifier + " " + gameAsset + " " + value.handle.path() + " " + assetTypeFromExtension);

		identifierGameAssetMap.put(gameAssetIdentifier, gameAsset);
		fileHandleGameAssetObjectMap.put(key, gameAsset);
	}

	public GameAsset createGameAssetForType (GameAssetType assetTypeFromExtension, String gameAssetIdentifier, RawAsset value) {
		if (!assetTypeFromExtension.isRootGameAsset()) {
			throw new GdxRuntimeException("Trying to load a game asset from a non root asset");
		}

		GameAsset gameAssetOut = null;


		try {
			switch (assetTypeFromExtension) {
			case SPRITE:
				GameAsset<Texture> textureGameAsset = new GameAsset<>(gameAssetIdentifier, assetTypeFromExtension);
				gameAssetOut = textureGameAsset;

				textureGameAsset.setResourcePayload(new Texture(value.handle));
				value.gameAssetReferences.add(textureGameAsset);

				textureGameAsset.dependentRawAssets.add(value);

				break;
			case ATLAS:
				GameAsset<TextureAtlas> textureAtlasGameAsset = new GameAsset<>(gameAssetIdentifier, assetTypeFromExtension);
				gameAssetOut = textureAtlasGameAsset;

				TextureAtlas.TextureAtlasData textureAtlasData = new TextureAtlas.TextureAtlasData(value.handle, value.handle.parent(), false);
				TextureAtlas atlas = new TextureAtlas(textureAtlasData);
				textureAtlasGameAsset.setResourcePayload(atlas);

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
					SkeletonData skeletonData = skeletonBinary.readSkeletonData(value.handle);
					skeletonDataGameAsset.setResourcePayload(skeletonData);

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

				break;
			case SOUND:
				break;
			case VFX:
				break;
			case VFX_OUTPUT:
				break;
			case SCRIPT:
				break;
			case PREFAB:
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

	private void collectRawResourceFromDirectory (FileHandle dir, boolean checkGameResources) {

		if (!dir.isDirectory()) {
			rawAssetCreated(dir, false);
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

	public void rawAssetCreated (FileHandle fileHandle, boolean checkGameResources) {
		RawAsset rawAsset = new RawAsset(fileHandle);

		FileHandle metadataHandleFor = AssetImporter.getMetadataHandleFor(fileHandle);
		if (metadataHandleFor.exists()) {
			try {
				GameAssetType assetTypeFromExtension = GameAssetType.getAssetTypeFromExtension(fileHandle.extension());
				Class<? extends AMetadata> metaClassForType = GameAssetType.getMetaClassForType(assetTypeFromExtension);
				rawAsset.metaData = json.fromJson(metaClassForType, metadataHandleFor);
			} catch (Exception e) {

				System.out.println("Error reading meta for " + metadataHandleFor.path() + " " + metadataHandleFor.readString());

				rawAsset.metaData = createMetaDataForAsset(rawAsset);

			}
		} else {
			rawAsset.metaData = createMetaDataForAsset(rawAsset);
		}


		uuidRawAssetMap.put(rawAsset.metaData.uuid, rawAsset);
		fileHandleRawAssetMap.put(fileHandle, rawAsset);

		if (checkGameResources) {
			checkGameAssetCreation();
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

			return metaForType;
		}
	}

	public <T> GameAsset<T> getAssetForPath (FileHandle file, Class<T> resourceClass) {
		String nameWithoutExtension = file.nameWithoutExtension();

		if (identifierGameAssetMap.containsKey(nameWithoutExtension)) {
			GameAsset gameAsset = identifierGameAssetMap.get(nameWithoutExtension);
			if (!gameAsset.isBroken()) {
				if (gameAsset.getResource().getClass().isAssignableFrom(resourceClass)) {
					return identifierGameAssetMap.get(nameWithoutExtension);
				}
			}
		}

		if (fileHandleGameAssetObjectMap.containsKey(file)) {
			GameAsset gameAsset = fileHandleGameAssetObjectMap.get(file);
			if (!gameAsset.isBroken()) {
				if (gameAsset.getResource().getClass().isAssignableFrom(resourceClass)) {
					return gameAsset;
				}
			}
		}

		GameAsset<T> tGameAsset = new GameAsset<>(file.nameWithoutExtension(), GameAssetType.SPRITE);
		tGameAsset.setBroken(new Exception("Broken asset"));
		return tGameAsset;
	}

	private <T> GameAsset<T> getDefaultGameAsset (Class<T> resourceClass) {
		System.out.println("Getting default game asset for " + resourceClass);
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
					identifierGameAssetMap.remove(gameAsset.nameIdentifier);
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


					AssetPathChanged assetPathChanged = Notifications.obtainEvent(AssetPathChanged.class);
					assetPathChanged.oldRelativePath = relative(file);
					assetPathChanged.newRelativePath = relative(destination);
					Notifications.fireEvent(assetPathChanged);

					if (isRootGameResource(rawAsset)) {
						//We need to update the game assets identifier
						String gameAssetIdentifierFromRawAsset = getGameAssetIdentifierFromRawAsset(rawAsset);

						if (identifierGameAssetMap.containsKey(gameAssetIdentifierFromRawAsset)) {
							GameAsset removedGameAsset = identifierGameAssetMap.remove(gameAssetIdentifierFromRawAsset);
							identifierGameAssetMap.put(destination.nameWithoutExtension(), removedGameAsset);

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
	}


	private boolean isRootGameResource (RawAsset rawAsset) {
		GameAssetType assetTypeFromExtension = GameAssetType.getAssetTypeFromExtension(rawAsset.handle.extension());
		return assetTypeFromExtension.isRootGameAsset();
	}

}
