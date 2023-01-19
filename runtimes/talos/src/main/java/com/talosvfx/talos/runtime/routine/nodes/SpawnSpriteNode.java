package com.talosvfx.talos.runtime.routine.nodes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.routine.RoutineNode;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.components.SpriteRendererComponent;
import com.talosvfx.talos.runtime.scene.components.TransformComponent;
import com.talosvfx.talos.runtime.utils.NamingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpawnSpriteNode extends RoutineNode {

    private static final Logger logger = LoggerFactory.getLogger(SpawnSpriteNode.class);

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

            logger.error("REDO THIS");
//            SceneData sceneData = SharedResources.currentProject.getSceneData();
//            Array<SceneLayer> renderLayers = sceneData.getRenderLayers();
//            spriteRendererComponent.sortingLayer = renderLayers.get(1); // todo: this is temporary

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
