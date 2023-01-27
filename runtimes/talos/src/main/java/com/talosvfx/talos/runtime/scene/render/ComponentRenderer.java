package com.talosvfx.talos.runtime.scene.render;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.GameObjectRenderer;
import com.talosvfx.talos.runtime.scene.components.RendererComponent;

public abstract class ComponentRenderer <T extends RendererComponent> {

	protected final GameObjectRenderer gameObjectRenderer;

	public ComponentRenderer (GameObjectRenderer gameObjectRenderer) {
		this.gameObjectRenderer = gameObjectRenderer;
	}

	public abstract void render (Batch batch, Camera camera, GameObject parent, T rendererComponent);
}
