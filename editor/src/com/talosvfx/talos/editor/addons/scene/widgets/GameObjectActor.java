package com.talosvfx.talos.editor.addons.scene.widgets;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Scaling;
import com.talosvfx.talos.editor.addons.scene.MainRenderer;
import com.talosvfx.talos.runtime.scene.GameObject;import com.talosvfx.talos.editor.addons.scene.logic.componentwrappers.TransformComponent;

public class GameObjectActor extends Table {

	private final GameObject gameObject;
	private final GameObject gameObjectCopy;
	private final boolean shouldScaleToFit;
	private BoundingBox estimatedSize;

	private final MainRenderer uiRenderer;

	public GameObjectActor (MainRenderer mainRenderer, GameObject gameObject, GameObject copy, boolean shouldScaleToFit) {
		this.gameObject = gameObject;
		this.gameObjectCopy = copy;
		this.uiRenderer = mainRenderer;
		this.shouldScaleToFit = shouldScaleToFit;

		//we need to estimate the size of the object and try to fit it into the actors dimensions
	}



	@Override
	public void draw (Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);

		estimatedSize = this.gameObjectCopy.estimateSizeFromRoot();

		//Scale the root game object transform to fit in our actor size
		float width = getWidth();
		float height = getHeight();


		GameObject first = gameObject.getGameObjects().first();
		TransformComponent transform = first.getComponent(TransformComponent.class);

		float scalingFactor = 1;
		if (shouldScaleToFit) {
			//Lets use width/height and the estimated size to scale to fit into our actor's width and height
			Vector2 apply = Scaling.fit.apply(estimatedSize.getWidth(), estimatedSize.getHeight(), width, height);

			//This is our fit size in world units, so lets scale one of the dimensions to match this
			float scale = apply.x / estimatedSize.getWidth();

			transform.scale.set(scale, scale);
			transform.position.set(getX() + getWidth()/2f, getY() + getHeight()/2f);
		} else {
			//If we arent, we just set the root transform to the x,y and leave sizing
			transform.position.set(getX(), getY());
		}

		uiRenderer.update(gameObject);
		if (batch instanceof PolygonBatch) {
			uiRenderer.render((PolygonBatch)batch, new MainRenderer.RenderState(), gameObject);
		}

	}
}
