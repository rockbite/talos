package com.talosvfx.talos.runtime.scene.render;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.talosvfx.talos.runtime.routine.RoutineRenderer;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.GameObjectRenderer;
import com.talosvfx.talos.runtime.scene.components.RoutineRendererComponent;

public class RoutineComponentRenderer extends ComponentRenderer<RoutineRendererComponent<?>> {

	private final RoutineRenderer routineRenderer;

	public RoutineComponentRenderer (GameObjectRenderer gameObjectRenderer) {
		super(gameObjectRenderer);

		routineRenderer = new RoutineRenderer();
	}

	@Override
	public void render (Batch batch, Camera camera, GameObject parent, RoutineRendererComponent<?> rendererComponent) {

		routineRenderer.render(batch, camera, parent, rendererComponent);

	}
}
