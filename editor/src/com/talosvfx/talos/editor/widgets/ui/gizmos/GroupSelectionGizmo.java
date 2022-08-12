package com.talosvfx.talos.editor.widgets.ui.gizmos;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.SpriteRendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import com.talosvfx.talos.editor.addons.scene.widgets.gizmos.Gizmo;
import com.talosvfx.talos.editor.widgets.ui.ViewportWidget;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;

public class GroupSelectionGizmo extends Gizmo {

	private final ViewportWidget viewportWidget;

	private BoundingBox selectionBounds = new BoundingBox();

	public GroupSelectionGizmo (ViewportWidget widget) {
		this.viewportWidget = widget;
	}

	@Override
	public void act (float delta) {
		super.act(delta);

		calculateSelectionBounds();
	}

	@Override
	public void setSelected (boolean selected) {
		super.setSelected(selected);

	}

	@Override
	public void touchDown (float x, float y, int button) {
		super.touchDown(x, y, button);
	}

	@Override
	public void touchDragged (float x, float y) {
		super.touchDragged(x, y);
	}

	private void calculateSelectionBounds () {
		selectionBounds.clr();
		for (GameObject object : viewportWidget.selection) {
			//Do da bounds on the transforms or widgets

			if (object.hasComponent(TransformComponent.class)) {

				TransformComponent transformComponent = object.getComponent(TransformComponent.class);
				selectionBounds.ext(transformComponent.worldPosition.x, transformComponent.worldPosition.y, 0);


				if (object.hasComponent(SpriteRendererComponent.class)) {
					SpriteRendererComponent spriteRendererComponent = object.getComponent(SpriteRendererComponent.class);
					float x = transformComponent.worldPosition.x;
					float y = transformComponent.worldPosition.y;

					Vector2 spriteSize = spriteRendererComponent.size;
					float spriteHeight = spriteSize.y * transformComponent.worldScale.x;
					float spriteWidth = spriteSize.x * transformComponent.worldScale.y;
					float halfWidth = spriteWidth / 2;
					float halfHeight = spriteHeight / 2;

					selectionBounds.ext(x - halfWidth, y - halfHeight, 0);
					selectionBounds.ext(x - halfWidth, y + halfHeight, 0);
					selectionBounds.ext(x + halfWidth, y - halfHeight, 0);
					selectionBounds.ext(x + halfWidth, y + halfHeight, 0);
				}
			}

		}
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);

		if (selected && !viewportWidget.selection.isEmpty()) {

			float boundsWidth = selectionBounds.getWidth();
			float boundsHeight = selectionBounds.getHeight();
			float centerX = selectionBounds.getCenterX();
			float centerY = selectionBounds.getCenterY();
			drawCircle(new Vector2(centerX, centerY), batch);

			drawLine(batch, centerX - boundsWidth/2, centerY - boundsHeight/2, centerX - boundsWidth/2, centerY + boundsHeight/2, ColorLibrary.ORANGE);
			drawLine(batch, centerX + boundsWidth/2, centerY - boundsHeight/2, centerX + boundsWidth/2, centerY + boundsHeight/2, ColorLibrary.ORANGE);

			drawLine(batch, centerX - boundsWidth/2, centerY + boundsHeight/2, centerX + boundsWidth/2, centerY + boundsHeight/2, ColorLibrary.ORANGE);
			drawLine(batch, centerX - boundsWidth/2, centerY - boundsHeight/2, centerX + boundsWidth/2, centerY - boundsHeight/2, ColorLibrary.ORANGE);
		}


	}


	@Override
	protected void updateFromGameObject () {

	}

	@Override
	public GameObject getGameObject () {
		throw new GdxRuntimeException("Not supported for Mega gizmo");
	}

	@Override
	public boolean isControllingGameObject (GameObject gameObject) {
		return viewportWidget.selection.contains(gameObject);
	}

	@Override
	public int getPriority () {
		return 10;
	}
}
