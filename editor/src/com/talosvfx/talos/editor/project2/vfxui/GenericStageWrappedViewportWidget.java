package com.talosvfx.talos.editor.project2.vfxui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.PolygonBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.rockbite.bongo.engine.render.PolygonSpriteBatchMultiTextureMULTIBIND;
import com.talosvfx.talos.editor.utils.grid.property_providers.DynamicGridPropertyProvider;
import com.talosvfx.talos.editor.widgets.ui.ViewportWidget;
import lombok.Getter;

import java.util.function.Supplier;

public class GenericStageWrappedViewportWidget extends ViewportWidget<Stage> {

	@Getter
	private final Stage stage;

	public GenericStageWrappedViewportWidget (Actor actor) {
		super();

		Supplier<Camera> currentCameraSupplier = viewportViewSettings.getCurrentCameraSupplier();
		Camera camera = currentCameraSupplier.get();

		stage = new Stage(new ScreenViewport(camera), new PolygonSpriteBatchMultiTextureMULTIBIND());

		camera.position.set(0, 0, 0);
		if (camera instanceof OrthographicCamera) {
			((OrthographicCamera) camera).zoom = 2f;
		}
		camera.update();

		stage.addActor(actor);
		stage.setKeyboardFocus(actor);

		setWorldSize(1000);
	}

	@Override
	public void act (float delta) {
		Supplier<Camera> currentCameraSupplier = viewportViewSettings.getCurrentCameraSupplier();
		Camera camera = currentCameraSupplier.get();
		stage.getViewport().setCamera(camera);


		super.act(delta);

		Vector2 temp = new Vector2();
		temp.set(getX(), getY());
		localToScreenCoordinates(temp);
		float x1 = temp.x;
		float y1 = Gdx.graphics.getHeight() - temp.y;

		temp.set(getX() + getWidth(), getY() + getHeight());
		localToScreenCoordinates(temp);
		float x2 = temp.x;
		float y2 = Gdx.graphics.getHeight() - temp.y;

		int screenWidth = (int)(x2 - x1);
		int screenHeight = (int)(y2 - y1);
		stage.getViewport().setScreenBounds((int)x1, (int)y1, screenWidth, screenHeight);
 		stage.act();

	}

	@Override
	protected Stage getEventContext() {
		return stage;
	}

	@Override
	public void drawContent (PolygonBatch batch, float parentAlpha) {

		Supplier<Camera> currentCameraSupplier = viewportViewSettings.getCurrentCameraSupplier();
		Camera camera = currentCameraSupplier.get();

		gridPropertyProvider.setLineThickness(pixelToWorld(1.2f));
		((DynamicGridPropertyProvider)gridPropertyProvider).distanceThatLinesShouldBe = pixelToWorld(150);


		if (camera instanceof OrthographicCamera) {
			gridPropertyProvider.update((OrthographicCamera)camera, parentAlpha);
		}
		gridRenderer.drawGrid(batch, shapeRenderer);

		Array<Rectangle> stack = new Array<>();
		while (ScissorStack.peekScissors() != null) {
			stack.add(ScissorStack.popScissors());
		}

		batch.end();
		stage.draw();
 		batch.begin();

		int idx = stack.size - 1;
		while (idx >= 0) {
			ScissorStack.pushScissors(stack.get(idx));
			idx--;
		}
	}

	@Override
	public void initializeGridPropertyProvider () {
		gridPropertyProvider = new DynamicGridPropertyProvider();
		gridPropertyProvider.getBackgroundColor().set(0.1f, 0.1f, 0.1f, 1f);
	}
}
