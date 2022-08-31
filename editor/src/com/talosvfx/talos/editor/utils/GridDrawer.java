package com.talosvfx.talos.editor.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.talosvfx.talos.editor.addons.scene.SceneEditorWorkspace;
import com.talosvfx.talos.editor.widgets.ui.ViewportWidget;

public class GridDrawer {
//
//	private final ViewportWidget widget;
//	private final OrthographicCamera camera;
//	private final SceneEditorWorkspace.GridProperties gridProperties;
//	private ShapeRenderer shapeRenderer = new ShapeRenderer();
//
//	public boolean drawAxis = true;
//	public boolean highlightCursorHover = false;
//	public boolean highlightCursorSelect = false;
//
//	public GridDrawer (ViewportWidget widget, OrthographicCamera camera, SceneEditorWorkspace.GridProperties gridProperties) {
//		this.widget = widget;
//		this.camera = camera;
//		this.gridProperties = gridProperties;
//	}
//
//	public int getMouseCellX () {
//		float[] floats = gridProperties.sizeProvider.get();
//
//		float gridSizeX = floats[0];
//		float gridSizeY = floats[1];
//
//		int x = Gdx.input.getX();
//		int y = Gdx.input.getY();
//		Vector3 projected = widget.getTouchToWorld(x, y);
//
//		//Find the 'cell'
//		float projX = projected.x;
//		projX /= gridSizeX;
//		projX = MathUtils.floor(projX);
//		projX *= gridSizeX;
//
//		return (int)projX;
//	}
//
//	public Vector2 project (Vector2 position) {
//		float[] floats = gridProperties.sizeProvider.get();
//
//		float gridSizeX = floats[0];
//		float gridSizeY = floats[1];
//
//		final Vector3 projected = widget.getTouchToWorld(position.x, position.y);
//
//		// find cell x axis
//		float projX = projected.x;
//		projX /= gridSizeX;
//		projX = MathUtils.floor(projX);
//
//		// find cell y axis
//		float projY = projected.y;
//		projY /= gridSizeY;
//		projY = MathUtils.floor(projY);
//
//		position.x = (int) projX;
//		position.y = (int) projY;
//
//		return position;
//	}
//
//	public int getMouseCellY () {
//		float[] floats = gridProperties.sizeProvider.get();
//
//		float gridSizeX = floats[0];
//		float gridSizeY = floats[1];
//
//		int x = Gdx.input.getX();
//		int y = Gdx.input.getY();
//		Vector3 projected = widget.getTouchToWorld(x, y);
//
//		float projY = projected.y;
//		projY /= gridSizeY;
//		projY = MathUtils.floor(projY);
//		projY *= gridSizeY;
//
//		return (int)projY;
//	}
//
//	public void drawGrid () {
//
//
//		if (drawAxis) {
//			shapeRenderer.setColor(Color.GREEN);
//			shapeRenderer.line(camera.position.x - totalWidth / 2, 0, camera.position.x + totalWidth, 0);
//			shapeRenderer.setColor(Color.RED);
//			shapeRenderer.line(0, camera.position.y - totalHeight, 0, camera.position.y + totalHeight);
//		}
//
//		shapeRenderer.setColor(color);
//
//
//		int projX = getMouseCellX();
//		int projY = getMouseCellY();
//
//		if (highlightCursorHover) {
//			shapeRenderer.rect(projX, projY, gridSizeX, gridSizeY);
//		}
//
//		if (highlightCursorSelect) {
//			if (Gdx.input.isTouched()) {
//				shapeRenderer.set(ShapeRenderer.ShapeType.Filled);
//				shapeRenderer.rect(projX, projY, gridSizeX, gridSizeY);
//			}
//		}
//
//		shapeRenderer.end();
//		Gdx.gl.glDisable(GL20.GL_BLEND);
//	}
//
//	public void toLocalCell (Vector3 vec) {
//		float[] floats = gridProperties.sizeProvider.get();
//
//		float gridSizeX = floats[0];
//		float gridSizeY = floats[1];
//
//		//Find the 'cell'
//		float projX = vec.x;
//		projX /= gridSizeX;
//		projX = MathUtils.floor(projX);
//		projX *= gridSizeX;
//
//		float projY = vec.y;
//		projY /= gridSizeY;
//		projY = MathUtils.floor(projY);
//		projY *= gridSizeY;
//		vec.set(projX, projY, vec.z);
//	}
}
