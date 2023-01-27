package com.talosvfx.talos.runtime.scene.render;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.talosvfx.talos.runtime.maps.TalosMapRenderer;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.GameObjectRenderer;
import com.talosvfx.talos.runtime.scene.components.MapComponent;

public class MapComponentRenderer extends ComponentRenderer<MapComponent> {

	private TalosMapRenderer talosMapRenderer;

	public MapComponentRenderer (GameObjectRenderer gameObjectRenderer) {
		super(gameObjectRenderer);
		talosMapRenderer = new TalosMapRenderer();
	}

	@Override
	public void render (Batch batch, Camera camera, GameObject parent, MapComponent rendererComponent) {
		talosMapRenderer.render(gameObjectRenderer, batch, parent, rendererComponent);
	}
}
