package com.talosvfx.talos.runtime.routine.nodes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.spine.SkeletonData;
import com.talosvfx.talos.runtime.RuntimeContext;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.routine.RoutineNode;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.SceneLayer;
import com.talosvfx.talos.runtime.scene.components.SpineRendererComponent;
import com.talosvfx.talos.runtime.scene.components.TransformComponent;
import com.talosvfx.talos.runtime.utils.NamingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpawnSpineNode extends RoutineNode {

    private static final Logger logger = LoggerFactory.getLogger(SpawnSpineNode.class);

    private Vector2 tmp = new Vector2();
    private GameObject goRef;

    @Override
    public void receiveSignal(String portName) {

        GameObject target = (GameObject) routineInstanceRef.getSignalPayload();

        GameAsset<SkeletonData> asset = (GameAsset<SkeletonData>) fetchAssetValue("spine");

        if(asset != null) {
            tmp.setZero();
            goRef = new GameObject();
            String nm = fetchStringValue("name");
            if(nm == null || nm.isEmpty()) nm = "dynamicSpineGo";
            String name = NamingUtils.getNewName(nm, target.getAllGONames());
            goRef.setName(name);
            TransformComponent transformComponent = new TransformComponent();
            SpineRendererComponent spineRendererComponent = new SpineRendererComponent();
            goRef.addComponent(transformComponent);
            goRef.addComponent(spineRendererComponent);
            spineRendererComponent.setGameAsset(asset);
            spineRendererComponent.orderingInLayer = fetchIntValue("layerOrder");

            String layerName = fetchStringValue("layerName");
            SceneLayer layer = RuntimeContext.getInstance().sceneData.getSceneLayerByName(layerName);
            if (layer != null) {
                spineRendererComponent.sortingLayer = layer;
            } else {
                SceneLayer preferredSceneLayer = RuntimeContext.getInstance().sceneData.getPreferredSceneLayer();
                spineRendererComponent.sortingLayer = preferredSceneLayer;
            }

            target.addGameObject(goRef);

            Color color = fetchColorValue("color");
            spineRendererComponent.color.set(color);

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
