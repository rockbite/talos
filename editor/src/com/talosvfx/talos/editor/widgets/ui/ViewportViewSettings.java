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
import com.talosvfx.talos.editor.project2.apps.preferences.ViewportSettingPreferences;
import com.talosvfx.talos.editor.utils.CameraController;
import lombok.Data;
import lombok.Getter;

import com.talosvfx.talos.runtime.utils.Supplier;

@Data
public class ViewportViewSettings {

	private final ViewportWidget viewportWidget;
	private final BongoCameraController threeDimensionalCameraController;

	private OrthographicCamera orthographicCamera;
	private PerspectiveCamera perspectiveCamera;


	private final CameraController twoDimensionalCameraController;


	@Getter
	private boolean disableCamera;


	public void setDisableCamera (boolean disableCamera) {
		this.disableCamera = disableCamera;
	}


	private float fov = 69;
	private float near = 0f;
	private float far = 100;

	private float zoom = 1;

	private boolean is3D;
	private boolean positiveZUp;

	private boolean showAxis;

	private boolean showGrid = true;
	private boolean gridOnTop = false;

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

		orthographicCamera.position.set(0, 0, 5);
		orthographicCamera.lookAt(new Vector3(0, 0, 0));

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

	public float getWorldWidth () {
		return worldWidth;
	}

	public void setWorldWidth (float worldWidth) {
		this.worldWidth = worldWidth;
	}

	public void update (float aspect) {
		//clamp some stuff

		twoDimensionalCameraController.setCamera(currentCamera);
		threeDimensionalCameraController.setCamera(currentCamera);

		twoDimensionalCameraController.setDisabled(disableCamera);
		threeDimensionalCameraController.setDisabled(disableCamera);

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

	public void applyPreferences(ViewportPreferences viewportPreferences) {
		getCurrentCamera().position.set(viewportPreferences.cameraPos);
		if (viewportPreferences.cameraDirection != null) {
			getCurrentCamera().direction.set(viewportPreferences.cameraDirection);
		}
		setZoom(viewportPreferences.cameraZoom);

		if (viewportPreferences.getSettingPreferences() != null) {
			ViewportSettingPreferences settingPreferences = viewportPreferences.getSettingPreferences();
			setWorldWidth(settingPreferences.width);
			setFov(settingPreferences.fov);
			setNear(settingPreferences.near);
			setFar(settingPreferences.far);


			set3D(settingPreferences.is3D);
			setPositiveZUp(settingPreferences.positiveZUp);

			switch (settingPreferences.currentCameraType) {
				case PERSPECTIVE:
					setPerspective();
					break;
				case ORTHOGRAPHIC:
					setOrthographic();
					break;
			}

			setShowAxis(settingPreferences.isShowAxis());
			setShowGrid(settingPreferences.isShowGrid());
			setGridOnTop(settingPreferences.isGridOnTop());
			setGridSize(settingPreferences.getGridSize());
		}
	}

	public enum CurrentCameraType {
		ORTHOGRAPHIC,
		PERSPECTIVE
	}

	public void collectPreferences(ViewportPreferences viewportPreferences) {
		viewportPreferences.setCameraPos(new Vector3(getCurrentCamera().position));
		viewportPreferences.setCameraDirection(new Vector3(getCurrentCamera().direction));
		viewportPreferences.setCameraZoom(getZoom());

		ViewportSettingPreferences settingPreferences = new ViewportSettingPreferences();
		settingPreferences.setWidth(getWorldWidth());
		settingPreferences.setFov(getFov());
		settingPreferences.setNear(getNear());
		settingPreferences.setFar(getFar());

		settingPreferences.set3D(is3D);
		settingPreferences.setPositiveZUp(positiveZUp);

		if (currentCamera instanceof OrthographicCamera) {
			settingPreferences.setCurrentCameraType(CurrentCameraType.ORTHOGRAPHIC);
		} else if (currentCamera instanceof PerspectiveCamera) {
			settingPreferences.setCurrentCameraType(CurrentCameraType.PERSPECTIVE);
		}

		settingPreferences.setShowAxis(isShowAxis());
		settingPreferences.setShowGrid(isShowGrid());
		settingPreferences.setGridOnTop(isGridOnTop());
		settingPreferences.setGridSize(getGridSize());

		viewportPreferences.setSettingPreferences(settingPreferences);
	}

}
