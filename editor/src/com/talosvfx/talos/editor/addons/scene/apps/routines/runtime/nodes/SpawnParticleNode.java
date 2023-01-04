package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineNode;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.ParticleComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.SpineRendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import com.talosvfx.talos.editor.serialization.VFXProjectData;
import com.talosvfx.talos.editor.utils.NamingUtils;

public class SpawnParticleNode extends RoutineNode {

    private Vector2 tmp = new Vector2();

    @Override
    public void receiveSignal (String portName) {

        GameObject target = (GameObject) routineInstanceRef.getSignalPayload();

        GameAsset<VFXProjectData> asset = fetchAssetValue("particle");

        if (asset != null) {
            tmp.setZero();
            GameObject go = new GameObject();
            String name = NamingUtils.getNewName("dynamicParticleGo", target.getAllGONames());
            go.setName(name);
            TransformComponent transformComponent = new TransformComponent();
            ParticleComponent particleComponent = new ParticleComponent();
            go.addComponent(transformComponent);
            go.addComponent(particleComponent);
            particleComponent.setGameAsset(asset);

            target.addGameObject(go);

            routineInstanceRef.setSignalPayload(go);
            sendSignal("onComplete");
        }
    }
}
