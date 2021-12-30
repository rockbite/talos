package com.talosvfx.talos.editor.addons.scene;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.SpriteRendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;

public class MainRenderer {

    private TransformComponent transformComponent = new TransformComponent();
    private Vector2 vec = new Vector2();
    private Vector2[] points = new Vector2[4];

    private static final int LB = 0;
    private static final int LT = 1;
    private static final int RT = 2;
    private static final int RB = 3;

    public MainRenderer() {
        for (int i = 0; i < 4; i++) {
            points[i] = new Vector2();
        }
    }

    // todo: do fancier logic later
    public void render (Batch batch, GameObject gameObject) {

        if(gameObject.hasComponent(SpriteRendererComponent.class)) {
            SpriteRendererComponent spriteRenderer = gameObject.getComponent(SpriteRendererComponent.class);
            TransformComponent transformComponent = getWorldTransform(gameObject);

            vec.set(0, 0);
            transformComponent.localToWorld(gameObject, vec);
            Vector2 renderPosition = vec;

            if(spriteRenderer.getTexture() != null) {
                batch.setColor(Color.WHITE);

                batch.draw(spriteRenderer.getTexture(),
                        renderPosition.x - 0.5f, renderPosition.y - 0.5f,
                        0.5f, 0.5f,
                        1f, 1f,
                        transformComponent.scale.x, transformComponent.scale.y,
                        transformComponent.rotation);
            }
        }

        Array<GameObject> gameObjects = gameObject.getGameObjects();

        if(gameObjects != null) {
            for(int i = 0; i < gameObjects.size; i++) {
                GameObject child = gameObjects.get(i);
                render(batch, child);
            }
        }
    }

    private TransformComponent getWorldTransform(GameObject gameObject) {
        getWorldLocAround(gameObject, points[LB], -0.5f, -0.5f);
        getWorldLocAround(gameObject, points[LT],-0.5f, 0.5f);
        getWorldLocAround(gameObject, points[RT],0.5f, 0.5f);
        getWorldLocAround(gameObject, points[RB],0.5f, -0.5f);

        vec.set(points[RT]).sub(points[LB]).scl(0.5f).add(points[LB]); // midpoint
        transformComponent.position.set(vec);
        vec.set(points[RT]).sub(points[LB]);
        transformComponent.scale.set(points[RT].dst(points[LT]), points[RT].dst(points[RB]));
        vec.set(points[RT]).sub(points[LT]).angleDeg();
        transformComponent.rotation = vec.angleDeg();

        return transformComponent;
    }

    private Vector2 getWorldLocAround(GameObject gameObject, Vector2 point, float x, float y) {
        point.set(x, y);
        TransformComponent.localToWorld(gameObject, point);

        return point;
    }
}
