package com.talosvfx.talos.runtime.routine.nodes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.routine.RoutineNode;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.components.SpineRendererComponent;
import com.talosvfx.talos.runtime.scene.components.TransformComponent;
import com.talosvfx.talos.runtime.utils.NamingUtils;

public class SpawnSpineNode extends RoutineNode {

    private Vector2 tmp = new Vector2();

    @Override
    public void receiveSignal(String portName) {

        GameObject target = (GameObject) routineInstanceRef.getSignalPayload();

        GameAsset asset = fetchAssetValue("spine");

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
            target.addGameObject(go);

            Color color = fetchColorValue("color");
            spineRendererComponent.color.set(color);

            routineInstanceRef.setSignalPayload(go);
            sendSignal("onComplete");
        }
    }
}
