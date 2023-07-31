package com.talosvfx.talos.editor.addons.scene.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasSprite;
import com.badlogic.gdx.net.HttpStatus;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.*;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.utils.Toasts;
import com.talosvfx.talos.runtime.assets.*;
import com.talosvfx.talos.runtime.assets.meta.AtlasMetadata;
import com.talosvfx.talos.runtime.assets.meta.SpriteMetadata;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public class RepositoryOptimizer {
	private static final Logger logger = LoggerFactory.getLogger(RepositoryOptimizer.class);

	@Data
	public static class UnpackPayload {

		private String targetAtlasPath;
		private String imageDir;
		private String outDir;

		public UnpackPayload () {}

		public void set (String targetAtlas, String imageDir, String outDir) {
			this.targetAtlasPath = targetAtlas;
			this.imageDir = imageDir;
			this.outDir = outDir;
		}
	}

	@Data
	public static class PackPayload {

		private TexturePacker.Settings settings;
		private String input;
		private String output;
		private String packFileName;

		public PackPayload () {}

		public void set (TexturePacker.Settings settings, String input, String output, String packFileName) {
			this.settings = settings;
			this.input = input;
			this.output = output;
			this.packFileName = packFileName;
		}
	}

	@Data
	public static class ExportPayload {
		private Array<UnpackPayload> unpackPayloads = new Array<>();
		private PackPayload packPayload;
	}


	public static void startProcess (ObjectSet<GameAsset<?>> gameAssetsToExport, GameAssetsExportStructure gameAssetExportStructure, BaseAssetRepository.AssetRepositoryCatalogueExportOptions settings, Runnable runnable) {
		checkAndDownload(gameAssetsToExport, gameAssetExportStructure, settings, runnable);
	}

	private static void checkAndDownload (ObjectSet<GameAsset<?>> gameAssetsToExport, GameAssetsExportStructure gameAssetExportStructure, BaseAssetRepository.AssetRepositoryCatalogueExportOptions settings, Runnable runnable) {
		if (hasToolsBinary()) {
			logger.info("Has tools binary, processing");
			process(gameAssetsToExport, gameAssetExportStructure, settings, runnable);
			logger.info("End tools processing");
		} else {
			logger.info("Downloading tools");
			CompletableFuture<Void> download = download();
			download.whenComplete((unused, throwable) -> {
				if (throwable == null) {
					Gdx.app.postRunnable(new Runnable() {
						@Override
						public void run () {
							Toasts.getInstance().showInfoToast("Downloading external tools complete");
						}
					});
					process(gameAssetsToExport, gameAssetExportStructure, settings, runnable);
				} else {
					throw new GdxRuntimeException("Failure to download and process");
				}
			});

			try {
				download.get();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			} catch (ExecutionException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static CompletableFuture<Void> download () {
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run () {
				Toasts.getInstance().showInfoToast("Downloading external tools");
			}
		});

		CompletableFuture<Void> downloadFuture = new CompletableFuture<>();

		Net.HttpRequest httpRequest = new Net.HttpRequest();
		httpRequest.setMethod(Net.HttpMethods.GET);
		httpRequest.setUrl("https://oss.sonatype.org/content/repositories/snapshots/com/talosvfx/tools/2.0.0-SNAPSHOT/tools-2.0.0-20230703.134952-149.jar");
		Gdx.net.sendHttpRequest(httpRequest, new Net.HttpResponseListener() {
			@Override
			public void handleHttpResponse (Net.HttpResponse httpResponse) {
				HttpStatus status = httpResponse.getStatus();

				if (status.getStatusCode() == 200) {
					long length = Long.parseLong(httpResponse.getHeader("Content-Length"));

					// We're going to download the file to external storage, create the streams
					InputStream is = httpResponse.getResultAsStream();
					OutputStream os = getJarLocation().write(false);

					byte[] bytes = new byte[1024];
					int count = -1;
					long read = 0;
					try {
						// Keep reading bytes and storing them until there are no more.
						while ((count = is.read(bytes, 0, bytes.length)) != -1) {
							os.write(bytes, 0, count);
							read += count;

							// Update the UI with the download progress
							final int progress = ((int)(((double)read / (double)length) * 100));

							System.out.println("Download progress " + progress + "%");

							// Since we are downloading on a background thread, post a runnable to touch ui

						}

						downloadFuture.complete(null);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			@Override
			public void failed (Throwable t) {
				Gdx.app.postRunnable(new Runnable() {
					@Override
					public void run () {
						Toasts.getInstance().showErrorToast("Error downloading tools, " + t.getLocalizedMessage());
					}
				});
			}

			@Override
			public void cancelled () {
				Gdx.app.postRunnable(new Runnable() {
					@Override
					public void run () {
						Toasts.getInstance().showErrorToast("Tools download cancelled");
					}
				});
			}
		});

		return downloadFuture;
	}

	private static FileHandle getUserHomeTalosDir () {
		String userHome = System.getProperty("user.home");
		return Gdx.files.absolute(userHome).child("Talos");
	}

	private static FileHandle getJarLocation () {
		return getUserHomeTalosDir().child("Exports/binaries/tools-jar.jar");
	}

	private static boolean hasToolsBinary () {
		return getJarLocation().exists();
	}

	private static class TextureBucket {

		String identifier;

		Texture.TextureFilter magFilter;
		Texture.TextureFilter minFilter;

		ObjectSet<GameAsset<TextureAtlas>> atlasesToUnpack = new ObjectSet<>();
		ObjectSet<GameAsset<AtlasSprite>> texturesToPack = new ObjectSet<>();

		GameAsset<TextureAtlas> generatedTextureAtlas;

		TexturePacker.Settings packSettings;

		ObjectSet<GameAsset<?>> parentResourcesThatShareThisAtlas = new ObjectSet<>();
		GameAsset<?> parentAsset = null;
		boolean single;

		public boolean hasAllGameResourcesToRequireAsset (GameAsset asset) {
			if (parentAsset.nameIdentifier.equals(asset.nameIdentifier)) return true;
			return false;
		}

		public boolean hasAllGameResourcesToRequireAsset (ObjectSet<GameAsset<?>> gameResourcesThatRequireMe) {
			if (gameResourcesThatRequireMe.size != parentResourcesThatShareThisAtlas.size) return false;

			for (GameAsset<?> gameAsset : gameResourcesThatRequireMe) {
				if (!parentResourcesThatShareThisAtlas.contains(gameAsset)) {
					return false;
				}
			}
			return true;
		}

		public void debugLog (FileHandle handle) {
			for (GameAsset<?> resourcesThatShareThisAtlas : parentResourcesThatShareThisAtlas) {
				handle.writeString("Dep: " + resourcesThatShareThisAtlas.nameIdentifier +" " + resourcesThatShareThisAtlas.type+ "\n", true);
				System.out.println("dep: " + resourcesThatShareThisAtlas.nameIdentifier);

			}
			System.out.println("Dep: " + parentAsset.nameIdentifier + " " + parentAsset.type);

			for (GameAsset<AtlasSprite> atlasSpriteGameAsset : texturesToPack) {
				System.out.println("\t" + atlasSpriteGameAsset.nameIdentifier  + " " + atlasSpriteGameAsset.type);
				handle.writeString("\t" + atlasSpriteGameAsset.nameIdentifier + " " + atlasSpriteGameAsset.type + "\n", true);
			}
			for (GameAsset<TextureAtlas> atlasSpriteGameAsset : atlasesToUnpack) {
				System.out.println("\t" + atlasSpriteGameAsset.nameIdentifier + " " + atlasSpriteGameAsset.type);

				handle.writeString("\t" + atlasSpriteGameAsset.nameIdentifier + " " + atlasSpriteGameAsset.type + "\n", true);
			}}
	}

	public static void process (ObjectSet<GameAsset<?>> gameAssetsToExport, GameAssetsExportStructure gameAssetExportStructure, BaseAssetRepository.AssetRepositoryCatalogueExportOptions settings, Runnable runnable) {
		OrderedSet<TextureBucket> buckets = new OrderedSet<>();

		OrderedSet<GameAsset<TextureAtlas>> atlases = new OrderedSet<>();
		OrderedSet<GameAsset<AtlasSprite>> sprites = new OrderedSet<>();


		for (GameAsset<?> gameAsset : gameAssetsToExport) {
			if (gameAsset.type == GameAssetType.ATLAS) {
				atlases.add((GameAsset<TextureAtlas>)gameAsset);
			}
			if (gameAsset.type == GameAssetType.SPRITE) {
				sprites.add((GameAsset<AtlasSprite>)gameAsset);
			}
		}

		ArrayList<GameAsset> tempArrayForSort = new ArrayList<>();
		for (GameAsset<TextureAtlas> atlas : atlases) {
			tempArrayForSort.add(atlas);
		}
		tempArrayForSort.sort(new Comparator<GameAsset>() {
			@Override
			public int compare (GameAsset o1, GameAsset o2) {
				return o1.nameIdentifier.compareTo(o2.nameIdentifier);
			}
		});
		atlases.clear();
		for (GameAsset gameAsset : tempArrayForSort) {
			atlases.add(gameAsset);
		}

		tempArrayForSort.clear();
		for (GameAsset<AtlasSprite> sprite : sprites) {
			tempArrayForSort.add(sprite);
		}
		tempArrayForSort.sort(new Comparator<GameAsset>() {
			@Override
			public int compare (GameAsset o1, GameAsset o2) {
				return o1.nameIdentifier.compareTo(o2.nameIdentifier);
			}
		});
		sprites.clear();
		for (GameAsset gameAsset : tempArrayForSort) {
			sprites.add(gameAsset);
		}


		for (GameAsset<TextureAtlas> atlas : atlases) {
			TextureAtlas resource = atlas.getResource();
			TextureBucket bucket = findOrCreateBucket(atlas, resource, buckets);
			bucket.atlasesToUnpack.add(atlas);
		}
		for (GameAsset<AtlasSprite> sprite : sprites) {
			AtlasSprite resource = sprite.getResource();
			TextureBucket bucket = findOrCreateBucket(sprite, resource.getTexture(), buckets, (SpriteMetadata)sprite.getRootRawAsset().metaData);
			bucket.texturesToPack.add(sprite);
		}

		CompletableFuture<TextureBucket>[] futures = new CompletableFuture[buckets.size];

		// mark the age of most recent file, to optimize on the next export
		// mark it here, because assets may change while packing
		long ageOfYoungestAssetTmp = 0;
		for (GameAsset<AtlasSprite> sprite : sprites) {
			ageOfYoungestAssetTmp = Math.max(ageOfYoungestAssetTmp, sprite.getRootRawAsset().handle.lastModified());
		}
		for (GameAsset<TextureAtlas> atlas : atlases) {
			ageOfYoungestAssetTmp = Math.max(ageOfYoungestAssetTmp, atlas.getRootRawAsset().handle.lastModified());
		}
		final long ageOfYoungestAsset = ageOfYoungestAssetTmp;

		// check, if we can skip packing right away
		// packing can be skipped, if no atlas or texture files were modified in source and old packed files exist
		boolean canSkipPacking = false;
		if (settings.getExportPathHandle().child("assetExport.json").exists()) {
			JsonReader reader = new JsonReader();
			try {
				JsonValue exportInfo = reader.parse(settings.getExportPathHandle().child("assetExport.json"));
				long prevAgeOfYoungestAsset = exportInfo.getLong("ageOfYoungestAsset", 0);
				canSkipPacking = prevAgeOfYoungestAsset > 0 && ageOfYoungestAsset == prevAgeOfYoungestAsset;
			} catch (SerializationException e) {
				logger.warn("Bad old export, cannot skip packing ", e);
			}
		}

		canSkipPacking = false; //disable this

//		FileHandle local = Gdx.files.local("log.txt");
//		local.writeString("", false);
//		for (TextureBucket bucket : buckets) {
//			bucket.debugLog(local);
//		}

		logger.info("Ready to unpack");

		int i = 0;
		for (TextureBucket bucket : buckets) {
			futures[i] = CompletableFuture.supplyAsync(createUnpackAndPackTask(bucket, canSkipPacking));
			i++;
		}

		logger.info("Waiting for all futures");

		//for buckets unpack then repack
		CompletableFuture.allOf(futures).whenComplete((unused, throwable) -> {
			if (throwable != null) {
				throwable.printStackTrace();
				logger.error("Bucket failed", throwable);
			} else {

				try {
					for (TextureBucket bucket : buckets) {
						for (GameAsset<AtlasSprite> textureGameAsset : bucket.texturesToPack) {

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
					logger.error("Exception reencoding buckets", e);
				}

				// mark the age of most recent file, to optimize on the next export
				gameAssetExportStructure.ageOfYoungestAsset = ageOfYoungestAsset;

				Gdx.app.postRunnable(runnable);
			}
		});
	}

	private static Supplier<TextureBucket> createUnpackAndPackTask (TextureBucket bucket, boolean canSkipPackingFinal) {
		return new Supplier<TextureBucket>() {
			@Override
			public TextureBucket get () {
				boolean canSkipPacking = canSkipPackingFinal;

				// check if old pack exists
				ExportPayload exportPayload = new ExportPayload();

				FileHandle exportParent = getUserHomeTalosDir().child("Exports");
				String name = SharedResources.currentProject.getProjectDir().name();
				FileHandle workingDir = exportParent.child(name);
				FileHandle bucketDir = workingDir.child(bucket.identifier);
				FileHandle raws = bucketDir.child("raws");
				FileHandle result = bucketDir.child("packed");

				// extra measures to see if old packed files exist, and we can indeed skip packing process
				if (exportParent.exists() && workingDir.exists() && bucketDir.exists() && result.exists()) {
					boolean hasTextures = result.list(".png").length > 0;
					boolean hasAtlases = result.list(".atlas").length > 0;
					if (!(hasTextures && hasAtlases)) {
						canSkipPacking = false;
					}
				} else {
					canSkipPacking = false;
				}

				if (canSkipPacking) {
					System.out.println("Skip packing for bucket: " + result);
				}

				if (!canSkipPacking) {
					exportParent.mkdirs();

					if (bucketDir.exists()) {
						bucketDir.deleteDirectory();
					}
					bucketDir.mkdirs();

					raws.mkdirs();
					result.mkdirs();
				}

				for (GameAsset<AtlasSprite> textureGameAsset : bucket.texturesToPack) {
					textureGameAsset.getRootRawAsset().handle.copyTo(raws);
				}

				for (GameAsset<TextureAtlas> textureAtlasGameAsset : bucket.atlasesToUnpack) {
					String absolutePath = textureAtlasGameAsset.getRootRawAsset().handle.file().getAbsolutePath();
					String imagePathAbsoluteDir = textureAtlasGameAsset.getRootRawAsset().handle.parent().file().getAbsolutePath();

					UnpackPayload unpackPayload = new UnpackPayload();
					unpackPayload.set(absolutePath, imagePathAbsoluteDir, raws.file().getAbsolutePath());
					exportPayload.getUnpackPayloads().add(unpackPayload);

				}


				PackPayload packPayload = new PackPayload();
				packPayload.set(bucket.packSettings, raws.file().getAbsolutePath(), result.file().getAbsolutePath(), bucket.identifier);
				exportPayload.setPackPayload(packPayload);

				if (!canSkipPacking) {
					CompletableFuture<Void> completableFuture = invokeExternalTool(exportPayload);
					try {
						completableFuture.get();
					} catch (InterruptedException e) {
						logger.error("Interrupt in packing", e);
						throw new RuntimeException(e);
					} catch (ExecutionException e) {
						logger.error("Interrupt in packing", e);
						throw new RuntimeException(e);
					}
				}

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
	}

	private static CompletableFuture<Void> invokeExternalTool (ExportPayload exportPayload) {
		FileHandle jarLocation = getJarLocation();

		Json json = new Json();
		String payload = json.toJson(exportPayload);

		CompletableFuture<Void> objectCompletableFuture = new CompletableFuture<>();

		try {
			String absolutePathToJar = jarLocation.file().getAbsolutePath();

			FileHandle packingConfig = FileHandle.tempFile("packingConfig" + exportPayload.packPayload.packFileName);
			packingConfig.writeString(payload, false);

			String args = "java -cp " + absolutePathToJar + " " + "com.talosvfx.talos.tools.ExportOptimizer" + " " + packingConfig.path();

			Process process = Runtime.getRuntime().exec(args);

			InputStream inputStream = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(inputStream);
			InputStream errorStream = process.getErrorStream();
			InputStreamReader esr = new InputStreamReader(errorStream);

			int n1;
			char[] c1 = new char[1024];
			StringBuffer stableOutput = new StringBuffer();
			while ((n1 = isr.read(c1)) > 0) {
				stableOutput.append(c1, 0, n1);
			}
			logger.info("Output: {}", stableOutput.toString());

			int n2;
			char[] c2 = new char[1024];
			StringBuffer stableError = new StringBuffer();
			while ((n2 = esr.read(c2)) > 0) {
				stableError.append(c2, 0, n2);
			}
			logger.error("Error {}", stableError.toString());

			int i = process.waitFor();

			if (i != 0) {
				Toasts.getInstance().showErrorToast("Exception in packing, check logs");
				throw new GdxRuntimeException("Exception in packing");
			}
			objectCompletableFuture.complete(null);

		} catch (Exception e) {
			logger.error("FAiluren invoking external tool", e);
			throw new RuntimeException(e);
		}

		return objectCompletableFuture;
	}

	;

	private static TextureBucket findOrCreateBucket (GameAsset<AtlasSprite> gameAsset, Texture resource, ObjectSet<TextureBucket> buckets, SpriteMetadata metadata) {
		if (metadata.dontPack) {
			TextureBucket textureBucket = new TextureBucket();
			textureBucket.minFilter = resource.getMinFilter();
			textureBucket.magFilter = resource.getMagFilter();
			textureBucket.identifier = "talos-pack-" + buckets.size;
			textureBucket.packSettings = getSingleTexturePackSettings(textureBucket);
			textureBucket.single = true;
			textureBucket.parentAsset = gameAsset;
			buckets.add(textureBucket);
			return textureBucket;
		}

		return findOrCreateBucket(gameAsset, resource, buckets);
	}

	private static TextureBucket findOrCreateBucket (GameAsset<TextureAtlas> gameAsset, TextureAtlas resource, ObjectSet<TextureBucket> buckets) {
		Texture texture = resource.getTextures().first();
		return findOrCreateBucket(gameAsset, texture, buckets);
	}


	private static TextureBucket findOrCreateBucket (GameAsset<?> gameAsset, Texture resource, ObjectSet<TextureBucket> buckets) {

		ObjectIntMap<GameAsset<?>> gameResourcesThatRequireMe1 = gameAsset.getGameResourcesThatRequireMe();

		GameAsset<?> highest = null;
		int highestValue =  0;
		for (ObjectIntMap.Entry<GameAsset<?>> gameAssetEntry : gameResourcesThatRequireMe1) {
			int value = gameAssetEntry.value;
			GameAsset<?> key = gameAssetEntry.key;
			if (value > highestValue) {
				highest = key;
				highestValue = value;
			}
		}


		if (highest == null) {
			System.out.println("Warning asset has no dependencies, this is not going to be optimized " + gameAsset.nameIdentifier);
			highest = gameAsset;
		}

		//Make a hash out of this

		ObjectSet<TextureBucket> filteredBuckets = new ObjectSet<>();

		for (TextureBucket bucket : buckets) {
			if (bucket.single) continue;

			if (bucketMatchesTextureSettings(resource, bucket)) {
				filteredBuckets.add(bucket);
			}
		}

		for (TextureBucket filteredBucket : filteredBuckets) {
			if (filteredBucket.hasAllGameResourcesToRequireAsset(highest)) {
				return filteredBucket;
			}
		}



		//Must create a new bucket
		TextureBucket newBucketFromTextureSettings = createNewBucketFromTextureSettings(buckets, resource, highest);
		return newBucketFromTextureSettings;
	}

	private static boolean bucketMatchesTextureSettings (Texture texture, TextureBucket bucket) {
		Texture.TextureFilter minFilter = bucket.minFilter;
		Texture.TextureFilter magFilter = bucket.magFilter;

		Texture.TextureFilter incomingMin = texture.getMinFilter();
		Texture.TextureFilter incomingMag = texture.getMagFilter();

		if (minFilter == incomingMin && magFilter == incomingMag) {
			return true;
		}
		return false;
	}

	private static RepositoryOptimizer.TextureBucket createNewBucketFromTextureSettings (ObjectSet<TextureBucket> buckets, Texture texture, GameAsset<?> highest) {
		TextureBucket textureBucket = new TextureBucket();
		textureBucket.minFilter = texture.getMinFilter();
		textureBucket.magFilter = texture.getMagFilter();
		textureBucket.identifier = "talos-pack-" + buckets.size;
		textureBucket.packSettings = getDefaultPackSettings(textureBucket);
		textureBucket.parentAsset = highest;
		buckets.add(textureBucket);

		return textureBucket;
	}

	private static TexturePacker.Settings getSingleTexturePackSettings (TextureBucket bucket) {
		TexturePacker.Settings settings = new TexturePacker.Settings();
		settings.edgePadding = false;
		settings.duplicatePadding = false;
		settings.paddingX = 0;
		settings.paddingY = 0;
		settings.stripWhitespaceX = false;
		settings.stripWhitespaceY = false;
		settings.maxWidth = 2048;
		settings.maxHeight = 2048;
		settings.filterMag = bucket.magFilter;
		settings.filterMin = bucket.minFilter;
		settings.pot = false;
		return settings;
	}

	private static TexturePacker.Settings getDefaultPackSettings (TextureBucket bucket) {
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
		settings.pot = false;
		settings.scale = new float[]{Float.parseFloat(SharedResources.currentProject.getExportPackingScale())};
		return settings;
	}



}
