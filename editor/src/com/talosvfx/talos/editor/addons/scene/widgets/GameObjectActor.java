package com.talosvfx.talos.editor.addons.scene.widgets;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.talosvfx.talos.editor.addons.scene.MainRenderer;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;

public class GameObjectActor extends Actor {

	private final GameObject gameObject;
	private Rectangle estimatedSize;

	private final MainRenderer mainRenderer;

	public GameObjectActor (MainRenderer mainRenderer, GameObject gameObject) {
		this.gameObject = gameObject;
		this.mainRenderer = mainRenderer;

		//we need to estimate the size of the object and try to fit it into the actors dimensions
//		estimatedSize = this.gameObject.estimateSizeFromRoot();
	}

	@Override
	public void draw (Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);

		//Scale the root game object transform to fit in our actor size
	}
}
