package com.talosvfx.talos.editor.addons.scene;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.SpriteRendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;

public class MainRenderer {

    private Vector2 vec = new Vector2();

    // todo: do fancier logic later
    public void render (Batch batch, GameObject gameObject) {

        if(gameObject.hasComponent(SpriteRendererComponent.class)) {
            SpriteRendererComponent spriteRenderer = gameObject.getComponent(SpriteRendererComponent.class);
            TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);

            gameObject.getPosition(vec);
            Vector2 renderPosition = vec;

            if(spriteRenderer.texture != null) {
                batch.draw(spriteRenderer.texture,
                        renderPosition.x - 0.5f, renderPosition.y - 0.5f,
                        0.5f, 0.5f,
                        1f, 1f,
                        transformComponent.scale.x, transformComponent.scale.y,
                        transformComponent.rotation + 90,
                        true);
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
}
