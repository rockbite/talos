package com.talosvfx.talos.runtime.routine.nodes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.talosvfx.talos.runtime.RuntimeContext;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.routine.RoutineNode;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.SceneLayer;
import com.talosvfx.talos.runtime.scene.components.SpriteRendererComponent;
import com.talosvfx.talos.runtime.scene.components.TransformComponent;
import com.talosvfx.talos.runtime.utils.NamingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpawnSpriteNode extends RoutineNode {

    private static final Logger logger = LoggerFactory.getLogger(SpawnSpriteNode.class);

    private Vector2 tmp = new Vector2();
    private GameObject goRef;

    @Override
    public void receiveSignal(String portName) {

        GameObject target = (GameObject) routineInstanceRef.getSignalPayload();

        GameAsset asset = fetchAssetValue("sprite");

        if(asset != null) {
            tmp.setZero();
            goRef = new GameObject();
            String nm = fetchStringValue("name");
            if(nm == null || nm.isEmpty()) nm = "dynamicGo";
            String name = NamingUtils.getNewName(nm, target.getAllGONames());
            goRef.setName(name);
            TransformComponent transformComponent = new TransformComponent();
            SpriteRendererComponent spriteRendererComponent = new SpriteRendererComponent();
            goRef.addComponent(transformComponent);
            goRef.addComponent(spriteRendererComponent);
            spriteRendererComponent.setGameAsset(asset);
            spriteRendererComponent.orderingInLayer = fetchIntValue("layerOrder");

            String layerName = fetchStringValue("layerName");
            SceneLayer layer = RuntimeContext.getInstance().sceneData.getSceneLayerByName(layerName);
            if (layer != null) {
                spriteRendererComponent.sortingLayer = layer;
            } else {
                SceneLayer preferredSceneLayer = RuntimeContext.getInstance().sceneData.getPreferredSceneLayer();
                spriteRendererComponent.sortingLayer = preferredSceneLayer;
            }

            target.addGameObject(goRef);

            spriteRendererComponent.size.x = fetchFloatValue("width");
            spriteRendererComponent.size.y = fetchFloatValue("height");

            String mode = fetchStringValue("mode");
            if(mode == null) mode = "simple";

            spriteRendererComponent.renderMode = SpriteRendererComponent.RenderMode.simple.valueOf(mode);

            Color color = fetchColorValue("color");
            spriteRendererComponent.color.set(color);

            routineInstanceRef.setSignalPayload(goRef);
            sendSignal("onComplete");
        }
    }

    @Override
    public Object queryValue(String targetPortName) {
        if (targetPortName.equals("gameObject")) {
            return goRef;
        }
        return 0;
    }

    @Override
    public void reset () {
        super.reset();
        goRef = null;
    }
}
