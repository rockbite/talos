package com.talosvfx.talos.runtime.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.talosvfx.talos.runtime.RuntimeContext;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetLoader;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.runtime.assets.GdxAssetRepo;
import com.talosvfx.talos.runtime.assets.GdxAssetRepoLoader;
import com.talosvfx.talos.runtime.scene.GameObjectRenderer;
import com.talosvfx.talos.runtime.scene.Scene;
import com.talosvfx.talos.runtime.scene.render.RenderState;

public class AssetManagerPipelineTest extends ApplicationAdapter {

	private PolygonSpriteBatch batch;
	private OrthographicCamera camera;
	private GameAsset<Scene> scene;

	private GameObjectRenderer gameObjectRenderer;
	private RenderState renderState;
	private AssetManager assetManager;

	private boolean loaded;


	@Override
	public void create () {

		assetManager = new AssetManager();
		assetManager.setLoader(GdxAssetRepo.class, new GdxAssetRepoLoader(assetManager.getFileHandleResolver()));
		assetManager.setLoader(GameAsset.class, new GameAssetLoader(assetManager.getFileHandleResolver()));

		GdxAssetRepoLoader.GdxAssetRepoLoaderParam parameter = new GdxAssetRepoLoader.GdxAssetRepoLoaderParam();
		parameter.exportFile = Gdx.files.internal("talos/assetExport.json");

		assetManager.load("testProject", GdxAssetRepo.class, parameter);

		gameObjectRenderer = new GameObjectRenderer();



		batch = new PolygonSpriteBatch();

		camera = new OrthographicCamera(20, 20);
		camera.update();

		renderState = new RenderState();
	}

	private void loaded () {
		GdxAssetRepo assetRepo = assetManager.get("testProject", GdxAssetRepo.class);
		scene = assetRepo.getAssetForIdentifier("school-bus", GameAssetType.SCENE);
	}
	@Override
	public void render () {
		ScreenUtils.clear(0, 0, 0, 1f);



		boolean update = assetManager.update(1000);
		if (update && !loaded) {
			System.out.println("Finished");
			loaded = true;
			loaded();
		} else {
			if (!loaded) {
				System.out.println("Loading " + assetManager.getProgress());
			}
		}

		if (loaded) {
			batch.setProjectionMatrix(camera.combined);

			batch.begin();
			Scene scene = this.scene.getResource();
			gameObjectRenderer.update(scene.getSelfObject(), Gdx.graphics.getDeltaTime());
			gameObjectRenderer.buildRenderStateAndRender(batch, camera, renderState, scene.getSelfObject());
			batch.end();
		}



	}

	public static void main (String[] args) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setWindowedMode(500, 500);
		new Lwjgl3Application(new AssetManagerPipelineTest(), config);
	}

}
