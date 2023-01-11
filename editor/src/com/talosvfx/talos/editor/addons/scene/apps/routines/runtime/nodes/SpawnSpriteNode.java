package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineNode;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.SpriteRendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import com.talosvfx.talos.editor.utils.NamingUtils;

public class SpawnSpriteNode extends RoutineNode {

    private Vector2 tmp = new Vector2();

    @Override
    public void receiveSignal(String portName) {

        GameObject target = (GameObject) routineInstanceRef.getSignalPayload();

        GameAsset asset = fetchAssetValue("sprite");

        if(asset != null) {
            tmp.setZero();
            GameObject go = new GameObject();
            String name = NamingUtils.getNewName("dynamicGo", target.getAllGONames());
            go.setName(name);
            TransformComponent transformComponent = new TransformComponent();
            SpriteRendererComponent spriteRendererComponent = new SpriteRendererComponent();
            go.addComponent(transformComponent);
            go.addComponent(spriteRendererComponent);
            spriteRendererComponent.setGameAsset(asset);
            spriteRendererComponent.orderingInLayer = fetchIntValue("layerOrder");
            target.addGameObject(go);

            spriteRendererComponent.size.x = fetchFloatValue("width");
            spriteRendererComponent.size.y = fetchFloatValue("height");

            String mode = fetchStringValue("mode");
            if(mode == null) mode = "simple";

            spriteRendererComponent.renderMode = SpriteRendererComponent.RenderMode.simple.valueOf(mode);

            Color color = fetchColorValue("color");
            spriteRendererComponent.color.set(color);

            routineInstanceRef.setSignalPayload(go);
            sendSignal("onComplete");
        }
    }
}
