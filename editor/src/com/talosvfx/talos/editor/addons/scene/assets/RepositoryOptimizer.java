package com.talosvfx.talos.editor.addons.scene.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.tools.texturepacker.TextureUnpacker;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.runtime.assets.GameAssetsExportStructure;
import com.talosvfx.talos.runtime.assets.RawAsset;
import com.talosvfx.talos.runtime.assets.meta.AtlasMetadata;

import java.awt.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class RepositoryOptimizer {

	private static class TextureBucket {

		String identifier;

		Texture.TextureFilter magFilter;
		Texture.TextureFilter minFilter;


		ObjectSet<GameAsset<TextureAtlas>> atlasesToUnpack = new ObjectSet<>();
		ObjectSet<GameAsset<Texture>> texturesToPack = new ObjectSet<>();

		GameAsset<TextureAtlas> generatedTextureAtlas;

	}

	public static void process (ObjectSet<GameAsset<?>> gameAssetsToExport, GameAssetsExportStructure gameAssetExportStructure, Runnable runnable) {
		ObjectSet<TextureBucket> buckets = new ObjectSet<>();


		ObjectSet<GameAsset<TextureAtlas>> atlases = new ObjectSet<>();
		ObjectSet<GameAsset<Texture>> sprites = new ObjectSet<>();
		for (GameAsset<?> gameAsset : gameAssetsToExport) {
			if (gameAsset.type == GameAssetType.ATLAS) {
				atlases.add((GameAsset<TextureAtlas>)gameAsset);
			}
			if (gameAsset.type == GameAssetType.SPRITE) {
				sprites.add((GameAsset<Texture>)gameAsset);
			}
		}

		for (GameAsset<TextureAtlas> atlas : atlases) {
			TextureAtlas resource = atlas.getResource();
			TextureBucket bucket = findOrCreateBucket(resource, buckets);
			bucket.atlasesToUnpack.add(atlas);
		}
		for (GameAsset<Texture> sprite : sprites) {
			Texture resource = sprite.getResource();
			TextureBucket bucket = findOrCreateBucket(resource, buckets);
			bucket.texturesToPack.add(sprite);
		}

		CompletableFuture<TextureBucket>[] futures = new CompletableFuture[buckets.size];

		int i = 0;
		for (TextureBucket bucket : buckets) {
			futures[i] = CompletableFuture.supplyAsync(createUnpackAndPackTask(bucket));
			i++;
		}

		//for buckets unpack then repack
		CompletableFuture.allOf(futures).whenComplete((unused, throwable) -> {
			if (throwable != null) {
				throwable.printStackTrace();
			} else {

				try {
					for (TextureBucket bucket : buckets) {
						for (GameAsset<Texture> textureGameAsset : bucket.texturesToPack) {

							//Set nothing to export because its now in atlas
							for (RawAsset dependentRawAsset : textureGameAsset.dependentRawAssets) {
								dependentRawAsset.shouldExport = false;
							}

							textureGameAsset.dependentGameAssets.add(bucket.generatedTextureAtlas);
						}

						for (GameAsset<TextureAtlas> textureAtlasGameAsset : bucket.atlasesToUnpack) {
							gameAssetsToExport.remove(textureAtlasGameAsset);

							//Don't export these because its combined into another atlas now
							for (RawAsset dependentRawAsset : textureAtlasGameAsset.dependentRawAssets) {
								dependentRawAsset.shouldExport = false;
							}

						}

						//We need to find others that reference this asssset and update it to the generated bucket
						for (GameAsset<Texture> texturesToPack : bucket.texturesToPack) {
							//Update all other references
							for (GameAsset<?> gameAsset : gameAssetsToExport) {
								Array.ArrayIterator<GameAsset<?>> iterator = gameAsset.dependentGameAssets.iterator();
								boolean found = false;
								while (iterator.hasNext()) {
									GameAsset<?> next = iterator.next();
									if (next.getRootRawAsset().metaData.uuid.equals(texturesToPack.getRootRawAsset().metaData.uuid)) {
										iterator.remove();
										found = true;
									}
								}
								if (found) {
									gameAsset.dependentGameAssets.add(bucket.generatedTextureAtlas);
								}
							}
						}
						for (GameAsset<TextureAtlas> textureAtlasGameAsset : bucket.atlasesToUnpack) {
							//Update all other references
							for (GameAsset<?> gameAsset : gameAssetsToExport) {
								Array.ArrayIterator<GameAsset<?>> iterator = gameAsset.dependentGameAssets.iterator();
								boolean found = false;
								while (iterator.hasNext()) {
									GameAsset<?> next = iterator.next();
									if (next.getRootRawAsset().metaData.uuid.equals(textureAtlasGameAsset.getRootRawAsset().metaData.uuid)) {
										iterator.remove();
										found = true;
									}
								}
								if (found) {
									gameAsset.dependentGameAssets.add(bucket.generatedTextureAtlas);
								}
							}
						}

						gameAssetsToExport.add(bucket.generatedTextureAtlas);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				Gdx.app.postRunnable(runnable);
			}
		});
	}

	private static Supplier<TextureBucket> createUnpackAndPackTask (TextureBucket bucket) {
		return new Supplier<TextureBucket>() {
			@Override
			public TextureBucket get () {

				FileHandle exportParent = Gdx.files.local("Exports");
				exportParent.mkdirs();

				String name = SharedResources.currentProject.getProjectDir().name();

				FileHandle workingDir = exportParent.child(name);
				FileHandle bucketDir = workingDir.child(bucket.identifier);

				if (bucketDir.exists()) {
					bucketDir.deleteDirectory();
				}
				bucketDir.mkdirs();


				FileHandle raws = bucketDir.child("raws");
				FileHandle result = bucketDir.child("packed");
				raws.mkdirs();
				result.mkdirs();


				for (GameAsset<TextureAtlas> textureAtlasGameAsset : bucket.atlasesToUnpack) {

					String absolutePath = textureAtlasGameAsset.getRootRawAsset().handle.file().getAbsolutePath();
					String imagePathAbsoluteDir = textureAtlasGameAsset.getRootRawAsset().handle.parent().file().getAbsolutePath();
					TextureUnpacker.main(new String[]{absolutePath, imagePathAbsoluteDir, raws.file().getAbsolutePath()});

				}

				for (GameAsset<Texture> textureGameAsset : bucket.texturesToPack) {
					textureGameAsset.getRootRawAsset().handle.copyTo(raws);
				}

				TexturePacker.Settings settings = new TexturePacker.Settings();
				settings.edgePadding = true;
				settings.duplicatePadding = true;
				settings.paddingX = 2;
				settings.paddingY = 2;
				settings.stripWhitespaceX = true;
				settings.stripWhitespaceY = true;
				settings.maxWidth = 2048;
				settings.maxHeight = 2048;
				settings.filterMag = bucket.magFilter;
				settings.filterMin = bucket.minFilter;

				TexturePacker.process(settings, raws.file().getAbsolutePath(), result.file().getAbsolutePath(), bucket.identifier);

				GameAsset<TextureAtlas> atlasGameAsset = new GameAsset<>(bucket.identifier, GameAssetType.ATLAS);
				atlasGameAsset.dependentRawAssets.add(new RawAsset(result.child(bucket.identifier + ".atlas")));

				FileHandle[] list = result.list();
				for (FileHandle fileHandle : list) {
					if (fileHandle.extension().equals("png")) {
						atlasGameAsset.dependentRawAssets.add(new RawAsset(fileHandle));
					}
				}


				atlasGameAsset.getRootRawAsset().metaData = new AtlasMetadata();
				atlasGameAsset.getRootRawAsset().metaData.uuid = UUID.randomUUID();
				bucket.generatedTextureAtlas = atlasGameAsset;



				return bucket;
			}
		};
	};

	private static TextureBucket findOrCreateBucket (Texture resource, ObjectSet<TextureBucket> buckets) {
		for (TextureBucket bucket : buckets) {
			Texture.TextureFilter minFilter = bucket.minFilter;
			Texture.TextureFilter magFilter = bucket.magFilter;

			Texture.TextureFilter incomingMin = resource.getMinFilter();
			Texture.TextureFilter incomingMag = resource.getMagFilter();

			if (minFilter == incomingMin && magFilter == incomingMag) {
				return bucket;
			}
		}
		TextureBucket textureBucket = new TextureBucket();
		textureBucket.minFilter = resource.getMinFilter();
		textureBucket.magFilter = resource.getMagFilter();
		textureBucket.identifier = "talos-pack-" + buckets.size;
		buckets.add(textureBucket);

		return textureBucket;
	}

	private static TextureBucket findOrCreateBucket (TextureAtlas resource, ObjectSet<TextureBucket> buckets) {
		for (TextureBucket bucket : buckets) {
			Texture.TextureFilter minFilter = bucket.minFilter;
			Texture.TextureFilter magFilter = bucket.magFilter;

			Texture.TextureFilter incomingMin = resource.getTextures().first().getMinFilter();
			Texture.TextureFilter incomingMag = resource.getTextures().first().getMagFilter();

			if (minFilter == incomingMin && magFilter == incomingMag) {
				return bucket;
			}
		}
		TextureBucket textureBucket = new TextureBucket();
		textureBucket.minFilter = resource.getTextures().first().getMinFilter();
		textureBucket.magFilter = resource.getTextures().first().getMagFilter();
		textureBucket.identifier = "talos-pack-" + buckets.size;
		buckets.add(textureBucket);

		return textureBucket;
	}

}
