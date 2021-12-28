package com.talosvfx.talos.editor.addons.scene;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.SpriteRendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;

public class MainRenderer {

    // todo: do fancier logic later
    public void render (Batch batch, GameObject gameObject) {

        if(gameObject.hasComponent(SpriteRendererComponent.class)) {
            SpriteRendererComponent spriteRenderer = gameObject.getComponent(SpriteRendererComponent.class);
            TransformComponent transformComponent = gameObject.getComponent(TransformComponent.class);

            if(spriteRenderer.texture != null) {
                batch.draw(spriteRenderer.texture, transformComponent.position.x - 0.5f, transformComponent.position.y - 0.5f, 1, 1);
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
