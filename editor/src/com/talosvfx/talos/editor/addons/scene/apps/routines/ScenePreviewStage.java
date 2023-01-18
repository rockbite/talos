package com.talosvfx.talos.editor.addons.scene.apps.routines;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.PolygonBatch;
import com.talosvfx.talos.editor.addons.scene.MainRenderer;

import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.scene.GameObject;import com.talosvfx.talos.runtime.scene.GameObjectContainer;

import com.talosvfx.talos.editor.notifications.Notifications;
import com.talosvfx.talos.editor.notifications.Observer;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.utils.grid.property_providers.DynamicGridPropertyProvider;
import com.talosvfx.talos.editor.widgets.ui.ViewportWidget;
import com.talosvfx.talos.runtime.scene.SavableContainer;
import com.talosvfx.talos.runtime.scene.Scene;
import com.talosvfx.talos.runtime.scene.components.CameraComponent;
import com.talosvfx.talos.runtime.scene.components.TransformComponent;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class ScenePreviewStage extends ViewportWidget implements Observer {

	private static final Logger logger = LoggerFactory.getLogger(ScenePreviewStage.class);

	@Getter
	public Scene currentScene;

	private MainRenderer renderer;

	private GameObject cameraGO;

	@Setter
	private float speed = 1;

	@Setter@Getter
	private boolean paused =false;
	private boolean lockCamera = false;

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

		if (camera instanceof OrthographicCamera) {
			renderer.setCamera((OrthographicCamera)camera);
		}

		drawMainRenderer(batch, parentAlpha);
	}

	@Override
	protected boolean canMoveAround () {
		return true;
	}

	@Override
	protected GameObjectContainer getEventContext() {
		return currentScene;
	}

	private void drawMainRenderer (PolygonBatch batch, float parentAlpha) {
		if (currentScene == null)
			return;

		if(cameraGO != null) {
			CameraComponent component = cameraGO.getComponent(CameraComponent.class);
			TransformComponent transform = cameraGO.getComponent(TransformComponent.class);
			Camera camera = renderer.getCamera();
			if(camera instanceof OrthographicCamera && lockCamera) {
				OrthographicCamera orthographicCamera = (OrthographicCamera) camera;
				orthographicCamera.position.set(transform.position.x, transform.position.y, 0);
				orthographicCamera.zoom = component.zoom;
				viewportViewSettings.setZoom(component.zoom);
			}
		}

		float currSpeed = speed;

		if(paused) currSpeed = 0;
		renderer.timeScale = currSpeed;

		renderer.setLayers(SharedResources.currentProject.getSceneData().getRenderLayers());
		renderer.update(currentScene.getSelfObject());
		renderer.render(batch, new MainRenderer.RenderState(), currentScene.getSelfObject());
	}

	@Override
	public void initializeGridPropertyProvider () {
		gridPropertyProvider = new DynamicGridPropertyProvider();
		gridPropertyProvider.getBackgroundColor().set(Color.BLACK);
	}

	public void setFromGameAsset(GameAsset<Scene> gameAsset) {
		if(gameAsset != null) {
			SavableContainer currentContainer = gameAsset.getResource();
			Scene scene = new Scene();
			scene.load(currentContainer.getAsString());

			currentScene = scene;
		}
	}

	public void setCameraGO(GameObject cameraGO) {
		this.cameraGO = cameraGO;
	}

	public void setLockCamera(boolean checked) {
		this.lockCamera = checked;
	}
}
