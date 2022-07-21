package com.talosvfx.talos.editor.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.talosvfx.talos.editor.widgets.ui.ViewportWidget;

public class GridDrawer {

	private static ShapeRenderer shapeRenderer = new ShapeRenderer();
	public static void drawGrid (ViewportWidget widget, Camera camera, Batch batch,
		float gridSizeX, float gridSizeY, int subdivisions,
		boolean highlightCursorHover, boolean highlightCursorSelect) {


		shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
		shapeRenderer.setTransformMatrix(batch.getTransformMatrix());

		float totalWidth = camera.viewportWidth;
		float totalHeight = camera.viewportHeight;

		if (camera instanceof OrthographicCamera) {
			totalWidth *= ((OrthographicCamera)camera).zoom;
			totalHeight *= ((OrthographicCamera)camera).zoom;
		}

		float leftSide = camera.position.x - totalWidth/2;
		float bottomSide = camera.position.y - totalHeight/2;

		float startGridX = ((int)(leftSide/gridSizeX)) * gridSizeX;
		float startGridY = ((int)(bottomSide/gridSizeY)) * gridSizeY;

		Color color = new Color(1, 1, 1, 0.2f);
		Color subDivisionColour = new Color(1, 1, 1, 0.1f);

		Gdx.gl.glEnable(GL20.GL_BLEND);
		shapeRenderer.setColor(color);
		shapeRenderer.setAutoShapeType(true);
		shapeRenderer.begin();


		for (float x = startGridX; x < startGridX + totalWidth; x+=gridSizeX) {
			shapeRenderer.line(x, bottomSide, x,  bottomSide + totalHeight);
		}
		for (float y = startGridY; y < startGridY + totalHeight; y+=gridSizeY) {
			shapeRenderer.line(leftSide, y, leftSide + totalWidth,  y);
		}
		if (subdivisions > 0) {
			float spacePerSubdivisionX = gridSizeX / (subdivisions + 1);
			float spacePerSubdivisionY = gridSizeY / (subdivisions + 1);

			shapeRenderer.setColor(subDivisionColour);
			for (float x = startGridX; x < startGridX + totalWidth; x+=gridSizeX) {
				for (int i = 0; i < subdivisions; i++) {
					shapeRenderer.line(x + spacePerSubdivisionX, bottomSide, x + spacePerSubdivisionX,  bottomSide + totalHeight);
				}
			}
			for (float y = startGridY; y < startGridY + totalHeight; y+=gridSizeY) {
				for (int i = 0; i < subdivisions; i++) {
					shapeRenderer.line(leftSide, y + spacePerSubdivisionY, leftSide + totalWidth,  y + spacePerSubdivisionY);
				}
			}
		}

		shapeRenderer.setColor(color);

		int x = Gdx.input.getX();
		int y = Gdx.input.getY();
		Vector3 projected = widget.getTouchToLocal(x, y);

		//Find the 'cell'
		float projX = projected.x;
		projX /= gridSizeX;
		projX = MathUtils.floor(projX);
		projX *= gridSizeX;

		float projY = projected.y;
		projY /= gridSizeY;
		projY = MathUtils.floor(projY);
		projY *= gridSizeY;

		if (highlightCursorHover) {
			shapeRenderer.rect(projX, projY, gridSizeX, gridSizeY);
		}

		if (highlightCursorSelect) {
			if (Gdx.input.isTouched()) {
				shapeRenderer.set(ShapeRenderer.ShapeType.Filled);
				shapeRenderer.rect(projX, projY, gridSizeX, gridSizeY);
			}
		}


		shapeRenderer.end();
		Gdx.gl.glDisable(GL20.GL_BLEND);

	}
}
