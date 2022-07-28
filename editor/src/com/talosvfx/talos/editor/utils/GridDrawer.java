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
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.widgets.ui.ViewportWidget;

public class GridDrawer {

	private final ViewportWidget widget;
	private final OrthographicCamera camera;
	private final SceneEditorWorkspace.GridProperties gridProperties;
	private ShapeRenderer shapeRenderer = new ShapeRenderer();

	public boolean highlightCursorHover = false;
	public boolean highlightCursorSelect = false;

	public GridDrawer (ViewportWidget widget, OrthographicCamera camera, SceneEditorWorkspace.GridProperties gridProperties) {
		this.widget = widget;
		this.camera = camera;
		this.gridProperties = gridProperties;
	}

	public int getMouseCellX () {
		float[] floats = gridProperties.sizeProvider.get();

		float gridSizeX = floats[0];
		float gridSizeY = floats[1];

		int x = Gdx.input.getX();
		int y = Gdx.input.getY();
		Vector3 projected = widget.getTouchToLocal(x, y);

		//Find the 'cell'
		float projX = projected.x;
		projX /= gridSizeX;
		projX = MathUtils.floor(projX);
		projX *= gridSizeX;

		return (int)projX;
	}

	public int getMouseCellY () {
		float[] floats = gridProperties.sizeProvider.get();

		float gridSizeX = floats[0];
		float gridSizeY = floats[1];

		int x = Gdx.input.getX();
		int y = Gdx.input.getY();
		Vector3 projected = widget.getTouchToLocal(x, y);

		float projY = projected.y;
		projY /= gridSizeY;
		projY = MathUtils.floor(projY);
		projY *= gridSizeY;

		return (int)projY;
	}

	public void drawGrid () {
		float[] floats = gridProperties.sizeProvider.get();

		float gridSizeX = floats[0];
		float gridSizeY = floats[1];
		int subdivisions = gridProperties.subdivisions;

		shapeRenderer.setProjectionMatrix(camera.combined);

		float totalWidth = camera.viewportWidth;
		float totalHeight = camera.viewportHeight;

		totalWidth *= camera.zoom;
		totalHeight *= camera.zoom;

		float leftSide = camera.position.x - totalWidth / 2;
		float bottomSide = camera.position.y - totalHeight / 2;

		float startGridX = ((int)(leftSide / gridSizeX)) * gridSizeX;
		float startGridY = ((int)(bottomSide / gridSizeY)) * gridSizeY;

		Color color = new Color(1, 1, 1, 0.2f);
		Color subDivisionColour = new Color(1, 1, 1, 0.1f);

		Gdx.gl.glEnable(GL20.GL_BLEND);
		shapeRenderer.setColor(color);
		shapeRenderer.setAutoShapeType(true);
		shapeRenderer.begin();


		for (float x = startGridX; x < startGridX + totalWidth; x += gridSizeX) {
			shapeRenderer.line(x, bottomSide, x, bottomSide + totalHeight);
		}
		for (float y = startGridY; y < startGridY + totalHeight; y += gridSizeY) {
			shapeRenderer.line(leftSide, y, leftSide + totalWidth, y);
		}
		if (subdivisions > 0) {
			float spacePerSubdivisionX = gridSizeX / (subdivisions + 1);
			float spacePerSubdivisionY = gridSizeY / (subdivisions + 1);

			shapeRenderer.setColor(subDivisionColour);
			for (float x = startGridX; x < startGridX + totalWidth; x += gridSizeX) {
				for (int i = 0; i < subdivisions; i++) {
					shapeRenderer.line(x + spacePerSubdivisionX, bottomSide, x + spacePerSubdivisionX, bottomSide + totalHeight);
				}
			}
			for (float y = startGridY; y < startGridY + totalHeight; y += gridSizeY) {
				for (int i = 0; i < subdivisions; i++) {
					shapeRenderer.line(leftSide, y + spacePerSubdivisionY, leftSide + totalWidth, y + spacePerSubdivisionY);
				}
			}
		}

		shapeRenderer.setColor(Color.GREEN);
		shapeRenderer.line(camera.position.x - totalWidth/2, 0, camera.position.x + totalWidth, 0);
		shapeRenderer.setColor(Color.RED);
		shapeRenderer.line(0, camera.position.y - totalHeight, 0, camera.position.y + totalHeight);

		shapeRenderer.setColor(color);


		int projX = getMouseCellX();
		int projY = getMouseCellY();

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

	public void toLocalCell (Vector3 vec) {
		float[] floats = gridProperties.sizeProvider.get();

		float gridSizeX = floats[0];
		float gridSizeY = floats[1];

		//Find the 'cell'
		float projX = vec.x;
		projX /= gridSizeX;
		projX = MathUtils.floor(projX);
		projX *= gridSizeX;

		float projY = vec.y;
		projY /= gridSizeY;
		projY = MathUtils.floor(projY);
		projY *= gridSizeY;
		vec.set(projX, projY, vec.z);
	}
}
