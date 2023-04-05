package com.talosvfx.talos.runtime.scene.render;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.GameObjectRenderer;
import com.talosvfx.talos.runtime.scene.components.CurveComponent;
import com.talosvfx.talos.runtime.scene.components.PathRendererComponent;
import com.talosvfx.talos.runtime.scene.components.TransformComponent;


public class PathComponentRenderer extends ComponentRenderer<PathRendererComponent>{
    Array<Vector2> points = new Array<>();
    public PathComponentRenderer(GameObjectRenderer gameObjectRenderer) {
        super(gameObjectRenderer);
    }

    @Override
    public void render(Batch batch, Camera camera, GameObject gameObject, PathRendererComponent rendererComponent) {
        TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);
        CurveComponent curveComponent = gameObject.getComponent(CurveComponent.class);

        points.clear();
        for (Vector2 point : curveComponent.points) {
            Vector2 finalPoint = point.cpy().add(transformComponent.worldPosition);

            points.add(finalPoint);
        }

        rendererComponent.setPoints(points);

        rendererComponent.drawMap(batch);
    }
}
