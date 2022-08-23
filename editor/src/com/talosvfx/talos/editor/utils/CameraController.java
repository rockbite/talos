/*******************************************************************************
 * Copyright 2019 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.talosvfx.talos.editor.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class CameraController extends InputAdapter {

	private Actor boundsProvider;

	private final OrthographicCamera camera;

	private Vector3 delta = new Vector3();

	private Vector3 screenSpaceTouchDown = new Vector3();
	private Vector3 screenSpaceCurrent = new Vector3();
	private Vector3 cameraStart = new Vector3();

	private boolean inverted = false;

	private boolean movingCamera = false;

	public boolean scrollOnly = false;

	public CameraController (OrthographicCamera camera) {
		this.camera = camera;
	}

	public void setBoundsProvider (Actor boundsProvider) {
		this.boundsProvider = boundsProvider;
	}

	public void setInvert (boolean inverted) {
		this.inverted = inverted;
	}

	@Override
	public boolean scrolled (float amountX, float amountY) {
		float currWidth = camera.viewportWidth * camera.zoom;
		float nextWidth = currWidth * (1f + amountY * 0.1f);
		float nextZoom = nextWidth/camera.viewportWidth;

		// snapping to one
		if(camera.zoom < 1 && nextZoom >= 1) nextZoom = 1f;
		if(camera.zoom > 1 && nextZoom <= 1) nextZoom = 1f;

		camera.zoom = nextZoom;

		camera.zoom = MathUtils.clamp(camera.zoom, 0.01f, 30f);

		return true;
	}

	@Override
	public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		if(scrollOnly) return false;

		movingCamera = false;
		if(button != 0) return false;
		movingCamera = true;
		screenSpaceTouchDown.set(screenX, screenY, 0);
		cameraStart.set(camera.position);
		return super.touchDown(screenX, screenY, pointer, button);
	}

	@Override
	public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		if(scrollOnly) return false;

		if(button != 0) return false;
		return super.touchUp(screenX, screenY, pointer, button);
	}

	@Override
	public boolean touchDragged (int screenX, int screenY, int pointer) {
		if(scrollOnly) return false;

		if(!movingCamera) return false;

		screenSpaceCurrent.set(screenX, screenY, 0);

		delta.set(screenSpaceCurrent).sub(screenSpaceTouchDown);
		//unit vector

		//Convert this screenspace delta into world space

		//Project 1 pixel into world space from the center
		Vector3 unproject1 = camera.unproject(new Vector3(0, 0, 0));
		Vector3 unproject = camera.unproject(new Vector3(1, 0, 0));

		Vector3 screenSpaceUnitToWorldUnit = unproject.sub(unproject1);

		if (boundsProvider != null) {
			float pixelWidth = boundsProvider.getWidth();
			float touchWidth = Gdx.graphics.getWidth();

			float scalingRatio = touchWidth/pixelWidth;
			delta.scl(scalingRatio);
		}

		delta.scl(-screenSpaceUnitToWorldUnit.x);

		camera.position.set(cameraStart).add(delta);
		camera.update();


		return super.touchDragged(screenX, screenY, pointer);
	}
}
