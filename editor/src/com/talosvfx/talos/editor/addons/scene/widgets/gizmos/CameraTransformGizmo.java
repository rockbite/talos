package com.talosvfx.talos.editor.addons.scene.widgets.gizmos;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.talosvfx.talos.editor.addons.scene.SceneUtils;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.editor.addons.scene.widgets.CameraPane;
import com.talosvfx.talos.editor.widgets.ui.common.ColorLibrary;
import com.talosvfx.talos.runtime.scene.components.CameraComponent;
import com.talosvfx.talos.runtime.scene.components.TransformComponent;

public class CameraTransformGizmo extends SmartTransformGizmo {

	private static CameraPane cameraPane;

	private Color borderColor = new Color();

	public CameraTransformGizmo () {
		if (cameraPane == null) {
			cameraPane = new CameraPane();
		}

		borderColor.set(ColorLibrary.FONT_GRAY);
		borderColor.a = 0.5f;
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		if(!gameObject.isEditorVisible()) return;

		rectPatch.scale(1f / prevScale, 1f / prevScale);
		rectPatch.scale(worldPerPixel, worldPerPixel);
		prevScale = worldPerPixel;

		if (gameObject.hasComponent(TransformComponent.class)) {
			if (selected) {
				for (int i = 0; i < 4; i++) {
					drawCircle(points[i], batch);
				}
			}
			drawLine(batch, points[LB], points[LT], borderColor);
			drawLine(batch, points[LT], points[RT], borderColor);
			drawLine(batch, points[RT], points[RB], borderColor);
			drawLine(batch, points[RB], points[LB], borderColor);
		}
	}

	@Override
	protected void updatePointsFromComponent () {
		getWorldLocAround(tmp, 0, 0); // this is center position of camera

		TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);
		CameraComponent cameraComponent = gameObject.getComponent(CameraComponent.class);

		points[LB].set(tmp.x - cameraComponent.size.x / 2f, tmp.y - cameraComponent.size.y / 2f);
		points[LT].set(tmp.x - cameraComponent.size.x / 2f, tmp.y + cameraComponent.size.y / 2f);
		points[RT].set(tmp.x + cameraComponent.size.x / 2f, tmp.y + cameraComponent.size.y / 2f);
		points[RB].set(tmp.x + cameraComponent.size.x / 2f, tmp.y - cameraComponent.size.y / 2f);

		points[LB].rotateAroundDeg(tmp, transformComponent.rotation);
		points[LT].rotateAroundDeg(tmp, transformComponent.rotation);
		points[RT].rotateAroundDeg(tmp, transformComponent.rotation);
		points[RB].rotateAroundDeg(tmp, transformComponent.rotation);

		tmp.set(points[RT]).sub(points[LB]).scl(0.5f).add(points[LB]); // midpoint
		updateRotationAreas(tmp.x, tmp.y);
	}

	@Override
	protected void transformOldToNew () {
		TransformComponent transform = gameObject.getComponent(TransformComponent.class);
		CameraComponent cameraComponent = gameObject.getComponent(CameraComponent.class);

		// bring old next points to local space
		for (int i = 0; i < 4; i++) {
			TransformComponent.worldToLocal(gameObject.parent, nextPoints[i]);
		}

		cameraComponent.size.set(nextPoints[RB].dst(nextPoints[LB]), nextPoints[LB].dst(nextPoints[LT]));
		cameraComponent.size = lowerPrecision(cameraComponent.size);
		tmp.set(nextPoints[RT]).sub(nextPoints[LB]).scl(0.5f).add(nextPoints[LB]); // this is midpoint
		transform.position = lowerPrecision(transform.position);
	}

	@Override
	protected void reportResizeUpdated (boolean isRapid) {
		TransformComponent transform = gameObject.getComponent(TransformComponent.class);
		SceneUtils.componentUpdated(gameObjectContainer, gameObject, transform, isRapid);

		CameraComponent camera = gameObject.getComponent(CameraComponent.class);
		SceneUtils.componentUpdated(gameObjectContainer, gameObject, camera, isRapid);
	}

	@Override
	protected void moveInLayerOrder (GameObject gameObject, int direction) {
		// do nothing
	}

	@Override
	public void setSelected (boolean selected) {
		super.setSelected(selected);

		if (selected) {
			viewport.addActor(cameraPane);
			cameraPane.setFrom(gameObjectContainer, gameObject);
		} else {
			cameraPane.remove();
		}
	}

	@Override
	public void notifyRemove () {
		cameraPane.remove();
	}
}
