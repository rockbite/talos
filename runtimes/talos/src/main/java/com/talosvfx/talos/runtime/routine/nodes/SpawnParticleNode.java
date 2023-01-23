package com.talosvfx.talos.runtime.routine.nodes;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.routine.RoutineNode;
import com.talosvfx.talos.runtime.routine.TickableNode;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.components.ParticleComponent;
import com.talosvfx.talos.runtime.scene.components.TransformComponent;
import com.talosvfx.talos.runtime.utils.NamingUtils;
import com.talosvfx.talos.runtime.vfx.ParticleEffectInstance;
import com.talosvfx.talos.runtime.vfx.serialization.BaseVFXProjectData;

public class SpawnParticleNode extends RoutineNode implements TickableNode {

    private Vector2 tmp = new Vector2();

    private Array<GameObject> trackingVFX = new Array();

    @Override
    public void receiveSignal (String portName) {

        GameObject target = (GameObject) routineInstanceRef.getSignalPayload();
        String parent = fetchStringValue("parent");
        if(parent != null && !parent.isEmpty()) {
            if(parent.equals("root")) {
                target = routineInstanceRef.getContainer().root;
            } else {
                Array<GameObject> gameObjects = routineInstanceRef.getContainer().findGameObjects(parent);
                if (!gameObjects.isEmpty()) {
                    target = gameObjects.first();
                }
            }
        }

        GameAsset<BaseVFXProjectData> asset = fetchAssetValue("particle");

        if (asset != null) {
            tmp.setZero();
            GameObject go = new GameObject();
            String name = NamingUtils.getNewName("dynamicParticleGo", target.getAllGONames());
            go.setName(name);
            TransformComponent transformComponent = new TransformComponent();
            transformComponent.position.set(fetchFloatValue("x"), fetchFloatValue("y"));
            ParticleComponent particleComponent = new ParticleComponent();
            go.addComponent(transformComponent);
            go.addComponent(particleComponent);
            particleComponent.setGameAsset(asset);
            particleComponent.orderingInLayer = fetchIntValue("layerOrder");

            target.addGameObject(go);

            trackingVFX.add(go);

            routineInstanceRef.setSignalPayload(go);
            sendSignal("onComplete");
        }
    }

    @Override
    public void tick(float delta) {
        for(int i = trackingVFX.size - 1; i >= 0 ; i--) {
            GameObject gameObject = trackingVFX.get(i);
            ParticleComponent component = gameObject.getComponent(ParticleComponent.class);
            if(component.getEffectRef() != null) {
                ParticleEffectInstance effectRef = component.getEffectRef();
                if(effectRef.getParticleCount() == 0) {
                    //todo put back in
                    trackingVFX.removeIndex(i);
                    gameObject.getParent().removeObject(gameObject);
                }
            }
        }
    }
}
