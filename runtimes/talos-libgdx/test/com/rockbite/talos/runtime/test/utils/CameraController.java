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

package com.rockbite.talos.runtime.test.utils;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class CameraController extends InputAdapter {

	private final OrthographicCamera camera;

	private Vector3 current = new Vector3();
	private Vector3 last = new Vector3();
	private Vector3 delta = new Vector3();


	public CameraController (OrthographicCamera camera) {
		this.camera = camera;
	}

	@Override
	public boolean scrolled (int amount) {

		camera.zoom += amount * 0.1f;
		camera.zoom = MathUtils.clamp(camera.zoom, 0.1f, 10f);

		return super.scrolled(amount);


	}

	@Override
	public boolean touchDown (int screenX, int screenY, int pointer, int button) {
		last.set(-1, -1, -1);
		return super.touchDown(screenX, screenY, pointer, button);
	}

	@Override
	public boolean touchUp (int screenX, int screenY, int pointer, int button) {
		return super.touchUp(screenX, screenY, pointer, button);
	}

	@Override
	public boolean touchDragged (int screenX, int screenY, int pointer) {
		camera.unproject(current.set(screenX, screenY, 0));
		if (!(last.x == -1 && last.y == -1 && last.z == -1)) {
			camera.unproject(delta.set(last.x, last.y, 0));
			delta.sub(current);
			camera.position.add(delta.x, delta.y, 0);
		}
		last.set(screenX, screenY, 0);
		return super.touchDragged(screenX, screenY, pointer);
	}
}
