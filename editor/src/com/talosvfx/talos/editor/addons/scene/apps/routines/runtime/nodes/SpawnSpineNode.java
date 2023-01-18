package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.SceneLayer;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineNode;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.SpineRendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.SpriteRendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import com.talosvfx.talos.editor.project2.SharedResources;
import com.talosvfx.talos.editor.project2.projectdata.SceneData;
import com.talosvfx.talos.editor.utils.NamingUtils;

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

            SceneData sceneData = SharedResources.currentProject.getSceneData();
            Array<SceneLayer> renderLayers = sceneData.getRenderLayers();
            spineRendererComponent.sortingLayer = renderLayers.get(1); // todo: this is temporary

            target.addGameObject(go);

            Color color = fetchColorValue("color");
            spineRendererComponent.color.set(color);

            routineInstanceRef.setSignalPayload(go);
            sendSignal("onComplete");
        }
    }
}
