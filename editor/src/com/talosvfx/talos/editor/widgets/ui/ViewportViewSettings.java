package com.talosvfx.talos.editor.widgets.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.rockbite.bongo.engine.camera.BongoCameraController;
import com.talosvfx.talos.editor.project2.apps.preferences.ViewportPreferences;
import com.talosvfx.talos.editor.utils.CameraController;
import lombok.Data;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Data
public class ViewportViewSettings {

	private final ViewportWidget viewportWidget;
	private final BongoCameraController threeDimensionalCameraController;

	private OrthographicCamera orthographicCamera;
	private PerspectiveCamera perspectiveCamera;


	private final CameraController twoDimensionalCameraController;


	private float fov = 69;
	private float near = 0f;
	private float far = 100;

	private float zoom = 1;

	private boolean is3D;
	private boolean positiveZUp;

	private boolean showAxis;

	private boolean showGrid = true;

	private float gridSize = 1;

	private Supplier<Camera> currentCameraSupplier;
	private Supplier<InputAdapter> currentCameraControllerSupplier;
	private float worldWidth = 10f;

	private Camera currentCamera;
	private InputAdapter currentCameraController;

	public ViewportViewSettings (ViewportWidget viewportWidget) {
		this.viewportWidget = viewportWidget;

		orthographicCamera = new OrthographicCamera(10, 10);
		perspectiveCamera = new PerspectiveCamera(fov, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		orthographicCamera.position.set(0, 0, 1);

		perspectiveCamera.position.set(0, 0, 5);
		perspectiveCamera.lookAt(new Vector3(0, 0, 0));

		orthographicCamera.update();
		perspectiveCamera.update();

		currentCameraSupplier = new Supplier<Camera>() {
			@Override
			public Camera get () {
				return currentCamera;
			}
		};

		currentCamera = orthographicCamera;


		twoDimensionalCameraController = new CameraController(orthographicCamera);
		twoDimensionalCameraController.setInvert(true);
		twoDimensionalCameraController.setBoundsProvider(viewportWidget);

		threeDimensionalCameraController = new BongoCameraController(orthographicCamera);
		threeDimensionalCameraController.translateTarget = true;
		threeDimensionalCameraController.setInvertedControls(true);

		currentCameraController = twoDimensionalCameraController;

		currentCameraControllerSupplier = new Supplier<InputAdapter>() {
			@Override
			public InputAdapter get () {
				return currentCameraController;
			}
		};
	}

	public void setPerspective () {
		this.currentCamera = perspectiveCamera;
	}

	public void setOrthographic () {
		this.currentCamera = orthographicCamera;
	}

	public void setWorldWidth (float worldWidth) {
		this.worldWidth = worldWidth;
	}

	public void update (float aspect) {
		//clamp some stuff

		twoDimensionalCameraController.setCamera(currentCamera);
		threeDimensionalCameraController.setCamera(currentCamera);

		if (is3D) {
			currentCameraController = threeDimensionalCameraController;
		} else {
			currentCameraController = twoDimensionalCameraController;
		}

		if (twoDimensionalCameraController == currentCameraController) {
//			twoDimensionalCameraController.update() has no update
		}
		if (threeDimensionalCameraController == currentCameraController) {
			threeDimensionalCameraController.update(); //Update is here
		}

		fov = MathUtils.clamp(fov, 0, 170);
		near = MathUtils.clamp(near, 0, 1000);
		far = MathUtils.clamp(far, near, 1000);


		perspectiveCamera.fieldOfView = fov;
		perspectiveCamera.near = near;
		perspectiveCamera.far = far;
		perspectiveCamera.viewportWidth = worldWidth;
		perspectiveCamera.viewportHeight = orthographicCamera.viewportWidth / aspect;
		perspectiveCamera.update();

		orthographicCamera.viewportWidth = worldWidth;
		orthographicCamera.near = near;
		orthographicCamera.far = far;
		orthographicCamera.zoom = zoom;
		orthographicCamera.viewportHeight = orthographicCamera.viewportWidth / aspect;

		orthographicCamera.update();
	}
}
