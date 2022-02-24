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

import com.badlogic.gdx.Files;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.rockbite.bongo.engine.render.ShaderFlags;
import com.rockbite.bongo.engine.render.ShaderSourceProvider;
import com.rockbite.bongo.engine.render.SpriteShaderCompiler;

public class GridRenderer extends Actor {

	private final Stage stage;
	private final OrthographicCamera camera;

	private Vector2 tmp = new Vector2();

	private ShapeRenderer shapeRenderer;

	public GridRenderer (Stage stage) {
		this.stage = stage;
		camera = (OrthographicCamera)this.stage.getViewport().getCamera();
		String shapeVertexSource = ShaderSourceProvider.resolveVertex("core/shape", Files.FileType.Classpath).readString();
		String shapeFragmentSource = ShaderSourceProvider.resolveFragment("core/shape", Files.FileType.Classpath).readString();

		shapeRenderer = new ShapeRenderer(5000,
			SpriteShaderCompiler.getOrCreateShader("core/shape", shapeVertexSource, shapeFragmentSource, new ShaderFlags())
		);
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
		batch.end();

		shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());

		final float worldWidth = stage.getViewport().getWorldWidth() * camera.zoom;
		final float worldHeight = stage.getViewport().getWorldHeight() * camera.zoom;

		final Vector3 position = stage.getViewport().getCamera().position;

		float x = position.x - worldWidth / 2f;
		float y = position.y - worldHeight / 2f;

		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		drawGrid(x, y, worldWidth, worldHeight);
		shapeRenderer.end();

		batch.begin();
	}

	private void drawGrid (float x, float y, float worldWidth, float worldHeight) {
		OrthographicCamera camera = (OrthographicCamera) getStage().getCamera();
		tmp.set(camera.position.x, camera.position.y);

		float tileSize = 15f;

		int lineCount = (int)(worldWidth / tileSize);
		int blackLineCount = (int)(worldWidth / (tileSize * 10));
		float width = worldWidth;
		float height = worldHeight;

		int iter = 0;
		float thickness = 3f;

		for (int i = -lineCount / 2 - 1; i < lineCount / 2 + 1; i++) {
			float spacing = width / lineCount;
			shapeRenderer.setColor(0.17f, 0.17f, 0.17f, 1f);
			thickness = 2f * camera.zoom;
			float posX = tmp.x - i * spacing - tmp.x % spacing;
			float posY = tmp.y + i * spacing - tmp.y % spacing;
			shapeRenderer.rectLine(posX, tmp.y - worldHeight/2f, posX, tmp.y + worldHeight/2f, thickness); // vertical
			shapeRenderer.rectLine(tmp.x - worldWidth/2f, posY, tmp.x + worldWidth/2f, posY, thickness); // horizontal
			iter++;
		}

		iter = 0;
		for (int i = -blackLineCount / 2 - 1; i < blackLineCount / 2 + 1; i++) {
			float spacing = width / blackLineCount;
			shapeRenderer.setColor(0.12f, 0.12f, 0.12f, 1f);
			thickness = 3f * camera.zoom;
			float posX = tmp.x - i * spacing - tmp.x % spacing;
			float posY = tmp.y + i * spacing - tmp.y % spacing;
			shapeRenderer.rectLine(posX, tmp.y - worldHeight/2f, posX, tmp.y + worldHeight/2f, thickness); // vertical
			shapeRenderer.rectLine(tmp.x - worldWidth/2f, posY, tmp.x + worldWidth/2f, posY, thickness); // horizontal
			iter++;
		}

/*
// white cross at center
        shapeRenderer.setColor(1f, 1f, 1f, 1f);
        shapeRenderer.rectLine(tmp.x-10, gridPos.y + worldHeight/2f, tmp.x+10, gridPos.y+worldHeight/2f, 2f);
        shapeRenderer.rectLine(gridPos.x+worldWidth/2f, tmp.y-10, gridPos.x+worldWidth/2f, tmp.y+10, 2f);
*/
	}
}
