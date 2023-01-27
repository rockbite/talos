package com.talosvfx.talos.runtime.routine.nodes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.spine.SkeletonData;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.routine.RoutineNode;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.components.SpineRendererComponent;
import com.talosvfx.talos.runtime.scene.components.TransformComponent;
import com.talosvfx.talos.runtime.utils.NamingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpawnSpineNode extends RoutineNode {

    private static final Logger logger = LoggerFactory.getLogger(SpawnSpineNode.class);

    private Vector2 tmp = new Vector2();

    @Override
    public void receiveSignal(String portName) {

        GameObject target = (GameObject) routineInstanceRef.getSignalPayload();

        GameAsset<SkeletonData> asset = (GameAsset<SkeletonData>) fetchAssetValue("spine");

        if(asset != null) {
            tmp.setZero();
            GameObject go = new GameObject();
            String nm = fetchStringValue("name");
            if(nm == null || nm.isEmpty()) nm = "dynamicSpineGo";
            String name = NamingUtils.getNewName(nm, target.getAllGONames());
            go.setName(name);
            TransformComponent transformComponent = new TransformComponent();
            SpineRendererComponent spineRendererComponent = new SpineRendererComponent();
            go.addComponent(transformComponent);
            go.addComponent(spineRendererComponent);
            spineRendererComponent.setGameAsset(asset);
            spineRendererComponent.orderingInLayer = fetchIntValue("layerOrder");

            logger.error("REDO THIS");
//            SceneData sceneData = SharedResources.currentProject.getSceneData();
//            Array<SceneLayer> renderLayers = sceneData.getRenderLayers();
//            spineRendererComponent.sortingLayer = renderLayers.get(1); // todo: this is temporary

            target.addGameObject(go);

            Color color = fetchColorValue("color");
            spineRendererComponent.color.set(color);

            routineInstanceRef.setSignalPayload(go);
            sendSignal("onComplete");
        }
    }
}
