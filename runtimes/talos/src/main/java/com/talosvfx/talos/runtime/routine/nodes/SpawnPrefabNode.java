package com.talosvfx.talos.runtime.routine.nodes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.talosvfx.talos.runtime.RuntimeContext;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.routine.RoutineNode;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.Prefab;
import com.talosvfx.talos.runtime.scene.SceneLayer;
import com.talosvfx.talos.runtime.scene.components.AComponent;
import com.talosvfx.talos.runtime.scene.components.RendererComponent;
import com.talosvfx.talos.runtime.scene.components.SpriteRendererComponent;
import com.talosvfx.talos.runtime.scene.components.TransformComponent;
import com.talosvfx.talos.runtime.utils.NamingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class SpawnPrefabNode extends RoutineNode {

    private static final Logger logger = LoggerFactory.getLogger(SpawnPrefabNode.class);

    private Vector2 tmp = new Vector2();
    private GameObject goRef;

    @Override
    public void receiveSignal(String portName) {

        GameObject target = (GameObject) routineInstanceRef.getSignalPayload();

        GameAsset<Prefab> asset = (GameAsset<Prefab>) fetchAssetValue("prefab");

        if(asset != null) {
            tmp.setZero();
            Prefab resource = asset.getResource();
            String nm = fetchStringValue("name");
            if(nm == null || nm.isEmpty()) nm = "dynamicGo";
            String name = NamingUtils.getNewName(nm, target.getAllGONames());

            Prefab copy = new Prefab(resource.getAsString(), name);
            goRef = copy.getSelfObject();
            goRef.uuid = UUID.randomUUID();

            int orderInLayer = fetchIntValue("layerOrder");

            String layerName = fetchStringValue("layerName");
            SceneLayer layer = RuntimeContext.getInstance().sceneData.getSceneLayerByName(layerName);
            if (layer == null) {
                layer = RuntimeContext.getInstance().sceneData.getPreferredSceneLayer();
            }

            for (AComponent component : goRef.getComponents()) {
                if (component instanceof RendererComponent) {
                    RendererComponent rendererComponent = (RendererComponent) component;

                    rendererComponent.orderingInLayer = orderInLayer;
                    rendererComponent.sortingLayer = layer;
                }
            }

            target.addGameObject(goRef);

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
