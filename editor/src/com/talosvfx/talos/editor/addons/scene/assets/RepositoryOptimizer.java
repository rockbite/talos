package com.talosvfx.talos.editor.addons.scene.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.net.HttpStatus;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.tools.texturepacker.TextureUnpacker;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectSet;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.utils.Toasts;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.runtime.assets.GameAssetsExportStructure;
import com.talosvfx.talos.runtime.assets.RawAsset;
import com.talosvfx.talos.runtime.assets.meta.AtlasMetadata;
import com.talosvfx.talos.tools.ExportOptimizer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public class RepositoryOptimizer {

	public static void startProcess (ObjectSet<GameAsset<?>> gameAssetsToExport, GameAssetsExportStructure gameAssetExportStructure, Runnable runnable) {
		checkAndDownload(gameAssetsToExport, gameAssetExportStructure, runnable);
	}

	private static void checkAndDownload (ObjectSet<GameAsset<?>> gameAssetsToExport, GameAssetsExportStructure gameAssetExportStructure, Runnable runnable) {
		if (hasToolsBinary()) {
			process(gameAssetsToExport, gameAssetExportStructure, runnable);
		} else {

			CompletableFuture<Void> download = download();
			download.whenComplete((unused, throwable) -> {
				if (throwable == null) {
					Gdx.app.postRunnable(new Runnable() {
						@Override
						public void run () {
							Toasts.getInstance().showInfoToast("Downloading external tools complete");
						}
					});
					process(gameAssetsToExport, gameAssetExportStructure, runnable);
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
		httpRequest.setUrl("https://oss.sonatype.org/content/repositories/snapshots/com/talosvfx/tools/2.0.0-SNAPSHOT/tools-2.0.0-20230327.113914-1.jar");
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

	private static FileHandle getJarLocation () {
		return Gdx.files.local("Exports/binaries/tools-jar.jar");
	}

	private static boolean hasToolsBinary () {
		return getJarLocation().exists();
	}

	private static class TextureBucket {

		String identifier;

		Texture.TextureFilter magFilter;
		Texture.TextureFilter minFilter;

		ObjectSet<GameAsset<TextureAtlas>> atlasesToUnpack = new ObjectSet<>();
		ObjectSet<GameAsset<AtlasRegion>> texturesToPack = new ObjectSet<>();

		GameAsset<TextureAtlas> generatedTextureAtlas;

	}

	public static void process (ObjectSet<GameAsset<?>> gameAssetsToExport, GameAssetsExportStructure gameAssetExportStructure, Runnable runnable) {

		ObjectSet<TextureBucket> buckets = new ObjectSet<>();

		ObjectSet<GameAsset<TextureAtlas>> atlases = new ObjectSet<>();
		ObjectSet<GameAsset<AtlasRegion>> sprites = new ObjectSet<>();
		for (GameAsset<?> gameAsset : gameAssetsToExport) {
			if (gameAsset.type == GameAssetType.ATLAS) {
				atlases.add((GameAsset<TextureAtlas>)gameAsset);
			}
			if (gameAsset.type == GameAssetType.SPRITE) {
				sprites.add((GameAsset<AtlasRegion>)gameAsset);
			}
		}

		for (GameAsset<TextureAtlas> atlas : atlases) {
			TextureAtlas resource = atlas.getResource();
			TextureBucket bucket = findOrCreateBucket(resource, buckets);
			bucket.atlasesToUnpack.add(atlas);
		}
		for (GameAsset<AtlasRegion> sprite : sprites) {
			AtlasRegion resource = sprite.getResource();
			TextureBucket bucket = findOrCreateBucket(resource.getTexture(), buckets);
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
						for (GameAsset<AtlasRegion> textureGameAsset : bucket.texturesToPack) {

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
				}

				Gdx.app.postRunnable(runnable);
			}
		});
	}

	private static Supplier<TextureBucket> createUnpackAndPackTask (TextureBucket bucket) {
		return new Supplier<TextureBucket>() {
			@Override
			public TextureBucket get () {
				ExportOptimizer.ExportPayload exportPayload = new ExportOptimizer.ExportPayload();

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

				for (GameAsset<AtlasRegion> textureGameAsset : bucket.texturesToPack) {
					textureGameAsset.getRootRawAsset().handle.copyTo(raws);
				}

				for (GameAsset<TextureAtlas> textureAtlasGameAsset : bucket.atlasesToUnpack) {
					String absolutePath = textureAtlasGameAsset.getRootRawAsset().handle.file().getAbsolutePath();
					String imagePathAbsoluteDir = textureAtlasGameAsset.getRootRawAsset().handle.parent().file().getAbsolutePath();

					ExportOptimizer.UnpackPayload unpackPayload = new ExportOptimizer.UnpackPayload();
					unpackPayload.set(absolutePath, imagePathAbsoluteDir, raws.file().getAbsolutePath());
					exportPayload.getUnpackPayloads().add(unpackPayload);

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

				ExportOptimizer.PackPayload packPayload = new ExportOptimizer.PackPayload();
				packPayload.set(settings, raws.file().getAbsolutePath(), result.file().getAbsolutePath(), bucket.identifier);
				exportPayload.setPackPayload(packPayload);

				CompletableFuture<Void> completableFuture = invokeExternalTool(exportPayload);
				try {
					completableFuture.get();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				} catch (ExecutionException e) {
					throw new RuntimeException(e);
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

	private static CompletableFuture<Void> invokeExternalTool (ExportOptimizer.ExportPayload exportPayload) {
		FileHandle jarLocation = getJarLocation();

		Json json = new Json();
		String payload = json.toJson(exportPayload);

		CompletableFuture<Void> objectCompletableFuture = new CompletableFuture<>();

		try {
			String absolutePathToJar = jarLocation.file().getAbsolutePath();

			//java -cp JarExample2.jar com.baeldung.jarArguments.JarExample "arg 1" arg2@
			Process process = Runtime.getRuntime().exec("java -cp " + absolutePathToJar + " " + ExportOptimizer.class.getName() + " " + payload);


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
			System.out.println("Output: " + stableOutput.toString());

			int n2;
			char[] c2 = new char[1024];
			StringBuffer stableError = new StringBuffer();
			while ((n2 = esr.read(c2)) > 0) {
				stableError.append(c2, 0, n2);
			}
			System.out.println("Error: " + stableError.toString());

			int i = process.exitValue();
			if (i != 0) {
				throw new GdxRuntimeException("Exception in packing");
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return objectCompletableFuture;
	}

	;

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
