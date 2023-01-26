package com.talosvfx.talos.runtime.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.talosvfx.talos.runtime.RuntimeContext;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.assets.GameAssetType;
import com.talosvfx.talos.runtime.assets.GdxAssetRepo;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.GameObjectRenderer;
import com.talosvfx.talos.runtime.scene.Scene;
import com.talosvfx.talos.runtime.scene.render.RenderState;

public class AssetPipelineTest extends ApplicationAdapter {

	private PolygonSpriteBatch batch;
	private OrthographicCamera camera;
	private GameAsset<Scene> scene;

	private GameObjectRenderer gameObjectRenderer;
	private RenderState renderState;

	@Override
	public void create () {
		RuntimeContext instance = RuntimeContext.getInstance();
		GdxAssetRepo gdxAssetRepo = new GdxAssetRepo();
		instance.setAssetRepository(gdxAssetRepo);
		gdxAssetRepo.loadBundleFromFile(Gdx.files.internal("testproject/assetExport.json"));

		gameObjectRenderer = new GameObjectRenderer();

		scene = gdxAssetRepo.getAssetForIdentifier("New Scene", GameAssetType.SCENE);


		batch = new PolygonSpriteBatch();

		camera = new OrthographicCamera(10, 10);
		camera.update();

		renderState = new RenderState();
	}

	@Override
	public void render () {
		ScreenUtils.clear(0, 0, 0, 1f);

		batch.setProjectionMatrix(camera.combined);

		batch.begin();

		Scene scene = this.scene.getResource();
		gameObjectRenderer.update(scene.getSelfObject(), Gdx.graphics.getDeltaTime());
		gameObjectRenderer.buildRenderStateAndRender(batch, renderState, scene.getSelfObject());
		batch.end();

	}

	public static void main (String[] args) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setWindowedMode(500, 500);
		new Lwjgl3Application(new AssetPipelineTest(), config);
	}

}
