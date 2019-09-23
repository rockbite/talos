package com.rockbite.tools.talos.editor.utils;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class GridRenderer extends Actor {

	private final Stage stage;
	private final OrthographicCamera camera;

	private Vector2 tmp = new Vector2();
	private Vector2 gridPos = new Vector2();

	private ShapeRenderer shapeRenderer;

	public GridRenderer (Stage stage) {
		this.stage = stage;
		camera = (OrthographicCamera)this.stage.getViewport().getCamera();
		shapeRenderer = new ShapeRenderer();
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

		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		drawGrid(x, y, worldWidth, worldHeight);
		shapeRenderer.end();

		batch.begin();
	}

	private void drawGrid (float x, float y, float worldWidth, float worldHeight) {
		OrthographicCamera camera = (OrthographicCamera) getStage().getCamera();
		gridPos.set(camera.position.x, camera.position.y);

		tmp.x = 0 + worldWidth/2f;
		tmp.y = 0 + worldHeight/2f;

		float tileSize = 15f;

		int lineCount = (int)(worldWidth / tileSize);
		int blackLineCount = (int)(worldWidth / (tileSize * 10));
		float width = worldWidth;
		float height = worldHeight;

		int iter = 0;
		float thickness = 3f;

		for (int i = -lineCount / 2 - 1; i < lineCount / 2 + 1; i++) {
			float spacing = width / lineCount;
			float offsetX = (int)(gridPos.x / spacing);
			float offsetY = (int)(gridPos.y / spacing);
			shapeRenderer.setColor(0.17f, 0.17f, 0.17f, 1f);
			thickness = 2f;
			float posX = tmp.x + (i - offsetX) * spacing;
			float posY = tmp.y + (i - offsetY) * spacing;
			shapeRenderer.rectLine(posX, 0, posX, worldHeight, thickness);
			shapeRenderer.rectLine(0, posY, worldWidth, posY, thickness);
			iter++;
		}

		iter = 0;
		for (int i = -blackLineCount / 2 - 1; i < blackLineCount / 2 + 1; i++) {
			float spacing = width / blackLineCount;
			float offsetX = (int)(gridPos.x / spacing);
			float offsetY = (int)(gridPos.y / spacing);
			shapeRenderer.setColor(0.12f, 0.12f, 0.12f, 1f);
			thickness = 3f;
			float posX = tmp.x + (i - offsetX) * spacing;
			float posY = tmp.y + (i - offsetY) * spacing;
			shapeRenderer.rectLine(posX, 0, posX, worldHeight, thickness);
			shapeRenderer.rectLine(0, posY, worldWidth, posY, thickness);
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
