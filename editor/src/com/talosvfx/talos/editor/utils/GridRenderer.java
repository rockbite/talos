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
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class GridRenderer {

	public float gridUnit;
	public float gridXStart;
	public float gridYStart;
	public float gridXEnd;
	public float gridYEnd;


	public void drawGrid (OrthographicCamera camera, Batch batch, ShapeRenderer shapeRenderer, float parentAlpha, float thickness, float distanceThatLinesShouldBe) {
		Gdx.gl.glLineWidth(1f);
		Gdx.gl.glEnable(GL20.GL_BLEND);

		float zeroAlpha = 0.8f;
		float mainLinesAlpha = 0.2f;
		float smallLinesAlpha = 0.1f;
		float linesToAppearAlpha = 0.01f;

		shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		Color gridColor = new Color(Color.GRAY);


		gridUnit = nextPowerOfTwo(distanceThatLinesShouldBe);

		float previousUnit = gridUnit / 2;
		linesToAppearAlpha = MathUtils.lerp(smallLinesAlpha, linesToAppearAlpha, (distanceThatLinesShouldBe - previousUnit) / (gridUnit - previousUnit));
		smallLinesAlpha = MathUtils.lerp(mainLinesAlpha, smallLinesAlpha, (distanceThatLinesShouldBe - previousUnit) / (gridUnit - previousUnit));

		int baseLineDivisor = 4;

		float visibleWidth = camera.viewportWidth * camera.zoom;
		float visibleHeight = camera.viewportHeight * camera.zoom;

		float cameraX = camera.position.x;
		float cameraY = camera.position.y;

		float visibleStartX = cameraX - visibleWidth / 2;
		float visibleStartY = cameraY - visibleHeight / 2;
		float visibleEndX = cameraX + visibleWidth / 2;
		float visibleEndY = cameraY + visibleHeight / 2;


		gridColor.a =  zeroAlpha * parentAlpha;
		shapeRenderer.setColor(gridColor);
		drawLine(shapeRenderer, batch, 0, cameraY - visibleHeight / 2, 0, cameraY + visibleHeight / 2, thickness, 0);
		drawLine(shapeRenderer, batch, visibleStartX, 0, visibleEndX, 0, thickness, 0);


		gridXStart = gridUnit * MathUtils.floor(visibleStartX / gridUnit) ;

		// drawing vertical lines
		for (float i = gridXStart; i < visibleEndX; i += gridUnit) {
			for (int j = 1; j <= baseLineDivisor; j++) {
				float smallUnitSize = gridUnit / baseLineDivisor;
				float x1 = i + j * smallUnitSize;

				for (int k = 1; k <= baseLineDivisor; k++) {
					float nextUnitSize = (gridUnit / baseLineDivisor) / baseLineDivisor;

					gridColor.a =  linesToAppearAlpha * parentAlpha;
					shapeRenderer.setColor(gridColor);
					drawLine(shapeRenderer, batch, x1 + k * nextUnitSize, cameraY - visibleHeight / 2, x1 + k * nextUnitSize, cameraY + visibleHeight / 2, thickness, j);
				}

				gridColor.a =  smallLinesAlpha * parentAlpha;
				shapeRenderer.setColor(gridColor);

				drawLine(shapeRenderer, batch, x1, cameraY - visibleHeight / 2, x1, cameraY + visibleHeight / 2, thickness, i);
			}


			if (i == 0) continue;
			gridColor.a =  mainLinesAlpha * parentAlpha;
			shapeRenderer.setColor(gridColor);
			drawLine(shapeRenderer, batch, i, cameraY - visibleHeight / 2, i, cameraY + visibleHeight / 2, thickness, i);
			gridXEnd = i;
		}

		gridYStart = gridUnit * MathUtils.floor(visibleStartY / gridUnit);

		// drawing vertical lines
		for (float i = gridYStart; i < visibleEndY; i += gridUnit) {
			for (int j = 1; j <= baseLineDivisor; j++) {
				float smallUnitSize = gridUnit / baseLineDivisor;
				float y1 = i + j * smallUnitSize;

				for (int k = 1; k <= baseLineDivisor; k++) {
					float nextUnitSize = (gridUnit / baseLineDivisor) / baseLineDivisor;

					gridColor.a =  linesToAppearAlpha * parentAlpha;
					shapeRenderer.setColor(gridColor);
					drawLine(shapeRenderer, batch, cameraX - visibleWidth / 2, y1 + k * nextUnitSize, cameraX + visibleWidth / 2, y1 + k * nextUnitSize, thickness, i);
				}

				gridColor.a =  smallLinesAlpha * parentAlpha;
				shapeRenderer.setColor(gridColor);
				drawLine(shapeRenderer, batch, cameraX - visibleWidth / 2, y1, cameraX + visibleWidth / 2, y1, thickness, i);
			}

			if (i == 0) continue;
			gridColor.a =  mainLinesAlpha * parentAlpha;
			shapeRenderer.setColor(gridColor);
			drawLine(shapeRenderer, batch, cameraX - visibleWidth / 2, i, cameraX + visibleWidth / 2,  i,  thickness, i);
			gridYEnd = i;
		}

		shapeRenderer.end();
	}

	private float nextPowerOfTwo (float value) {
		boolean negative = false;
		boolean smallerOne = false;
		if (value < 0) {
			negative = true;
			value *= -1;
		}

		if (value < 1 ) {
			value = 1 / value;
			smallerOne = true;
		}

		float unit = MathUtils.nextPowerOfTwo(MathUtils.ceil(value));
		if (smallerOne) {
			unit = 1 / unit;
			unit *= 2;
		}

		return unit;
	}

	private void drawLine (ShapeRenderer shapeRenderer, Batch batch, float x1, float y1, float x2, float y2, float thickness, float coord) {

		boolean debug = false;
		shapeRenderer.rectLine(x1, y1, x2, y2, thickness);

		if (debug) {
			BitmapFont bitmapFont = new BitmapFont();
			bitmapFont.getData().scale(1.5f);
			bitmapFont.setColor(1, 1, 1, 1);
			batch.begin();
			bitmapFont.draw(batch, " " + coord, x1, y1 + 30f);
			bitmapFont.draw(batch, " " + coord, x2 - 70f, y2);
			batch.flush();
			batch.end();
			bitmapFont.dispose();
		}
	}
}
