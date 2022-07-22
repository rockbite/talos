package com.talosvfx.talos.editor.addons.scene.widgets;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Scaling;
import com.talosvfx.talos.editor.addons.scene.MainRenderer;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import com.talosvfx.talos.editor.wrappers.ScopeModuleWrapper;

public class GameObjectActor extends Actor {

	private final GameObject gameObject;
	private final boolean shouldScaleToFit;
	private BoundingBox estimatedSize;

	private final MainRenderer mainRenderer;

	public GameObjectActor (MainRenderer mainRenderer, GameObject gameObject, boolean shouldScaleToFit) {
		this.gameObject = gameObject;
		this.mainRenderer = mainRenderer;
		this.shouldScaleToFit = shouldScaleToFit;

		//we need to estimate the size of the object and try to fit it into the actors dimensions
		estimatedSize = this.gameObject.estimateSizeFromRoot();
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);

		//Scale the root game object transform to fit in our actor size
		float width = getWidth();
		float height = getHeight();

		TransformComponent transform = gameObject.getComponent(TransformComponent.class);

		float scalingFactor = 1;
		if (shouldScaleToFit) {
			//Lets use width/height and the estimated size to scale to fit into our actor's width and height
			Vector2 apply = Scaling.fit.apply(estimatedSize.getWidth(), estimatedSize.getHeight(), width, height);

			//This is our fit size in world units, so lets scale one of the dimensions to match this
			float scale = estimatedSize.getWidth() / apply.x;
			transform.scale.x = transform.scale.y = scale;
			transform.position.set(getX(), getY());
		} else {
			//If we arent, we just set the root transform to the x,y and leave sizing
			transform.position.set(getX(), getY());
		}

		mainRenderer.render(batch, gameObject);
	}
}
