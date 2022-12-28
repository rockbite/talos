package com.talosvfx.talos.editor.addons.scene.apps.routines;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.PolygonBatch;
import com.talosvfx.talos.editor.addons.scene.MainRenderer;

import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.events.TweenFinishedEvent;
import com.talosvfx.talos.editor.addons.scene.events.TweenPlayedEvent;
import com.talosvfx.talos.editor.addons.scene.logic.SavableContainer;
import com.talosvfx.talos.editor.addons.scene.logic.Scene;

import com.talosvfx.talos.editor.notifications.EventHandler;
import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.Observer;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.utils.grid.property_providers.DynamicGridPropertyProvider;
import com.talosvfx.talos.editor.widgets.ui.ViewportWidget;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class ScenePreviewStage extends ViewportWidget implements Observer {

	private static final Logger logger = LoggerFactory.getLogger(ScenePreviewStage.class);

	@Getter
	public Scene currentScene;

	private MainRenderer renderer;

	private boolean isPlaying = false;

	public ScenePreviewStage () {
		setSkin(SharedResources.skin);
		setWorldSize(10);
		renderer = new MainRenderer();
		addActor(rulerRenderer);
		Notifications.registerObserver(this);


	}

	@Override
	public void drawContent (PolygonBatch batch, float parentAlpha) {
		batch.end();

		Supplier<Camera> currentCameraSupplier = viewportViewSettings.getCurrentCameraSupplier();
		Camera camera = currentCameraSupplier.get();

		gridPropertyProvider.setLineThickness(pixelToWorld(1.2f));
		((DynamicGridPropertyProvider)gridPropertyProvider).distanceThatLinesShouldBe = pixelToWorld(150);

		if (camera instanceof OrthographicCamera) {
			gridPropertyProvider.update((OrthographicCamera)camera, parentAlpha);
		}

		gridRenderer.drawGrid(batch, shapeRenderer);
		renderer.setRenderParentTiles(false);
		batch.begin();

		renderer.skipUpdates = !isPlaying;
		if (camera instanceof OrthographicCamera) {
			renderer.setCamera((OrthographicCamera)camera);
		}

		drawMainRenderer(batch, parentAlpha);
		renderer.skipUpdates = !isPlaying;
	}

	@Override
	protected boolean canMoveAround () {
		return isDragging;
	}

	private void drawMainRenderer (PolygonBatch batch, float parentAlpha) {
		if (currentScene == null)
			return;

		renderer.setLayers(SharedResources.currentProject.getSceneData().getRenderLayers());
		renderer.update(currentScene.getSelfObject());
		renderer.render(batch, new MainRenderer.RenderState(), currentScene.getSelfObject());
	}

	@Override
	public void initializeGridPropertyProvider () {
		gridPropertyProvider = new DynamicGridPropertyProvider();
		gridPropertyProvider.getBackgroundColor().set(Color.BLACK);
	}

	/*
	@EventHandler
	public void onTweenPlay (TweenPlayedEvent event) {
		updateWorkspaceState(true);
		isPlaying = true;
	}

	@EventHandler
	public void onTweenFinish (TweenFinishedEvent event) {
		updateWorkspaceState(false);
		isPlaying = false;
	}*/

	public void setFromGameAsset(GameAsset<Scene> gameAsset) {
		if(gameAsset != null) {
			SavableContainer currentContainer = gameAsset.getResource();
			Scene scene = new Scene();
			scene.load(currentContainer.getAsString());

			currentScene = scene;
		}
	}
}
