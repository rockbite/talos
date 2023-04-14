package com.talosvfx.talos.runtime.routine.nodes;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import com.talosvfx.talos.runtime.RuntimeContext;
import com.talosvfx.talos.runtime.assets.GameAsset;
import com.talosvfx.talos.runtime.routine.RoutineNode;
import com.talosvfx.talos.runtime.routine.TickableNode;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.SceneLayer;
import com.talosvfx.talos.runtime.scene.components.ParticleComponent;
import com.talosvfx.talos.runtime.scene.components.SpineRendererComponent;
import com.talosvfx.talos.runtime.scene.components.TransformComponent;
import com.talosvfx.talos.runtime.utils.NamingUtils;
import com.talosvfx.talos.runtime.vfx.ParticleEffectInstance;
import com.talosvfx.talos.runtime.vfx.serialization.BaseVFXProjectData;

public class SpawnParticleNode extends RoutineNode implements TickableNode {

    private Vector2 tmp = new Vector2();

    private Array<GameObject> trackingVFX = new Array();
    private GameObject goRef;

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

        GameAsset<BaseVFXProjectData> asset = (GameAsset<BaseVFXProjectData>) fetchAssetValue("particle");

        if (asset != null) {
            tmp.setZero();
            goRef = new GameObject();
            String name = NamingUtils.getNewName("dynamicParticleGo", target.getAllGONames());
            goRef.setName(name);
            TransformComponent transformComponent = new TransformComponent();
            transformComponent.position.set(fetchFloatValue("x"), fetchFloatValue("y"));
            ParticleComponent particleComponent = new ParticleComponent();
            goRef.addComponent(transformComponent);
            goRef.addComponent(particleComponent);
            particleComponent.setGameAsset(asset);
            particleComponent.orderingInLayer = fetchIntValue("layerOrder");

            String layerName = fetchStringValue("layerName");
            SceneLayer layer = RuntimeContext.getInstance().sceneData.getSceneLayerByName(layerName);
            if (layer != null) {
                particleComponent.sortingLayer = layer;
            } else {
                SceneLayer preferredSceneLayer = RuntimeContext.getInstance().sceneData.getPreferredSceneLayer();
                particleComponent.sortingLayer = preferredSceneLayer;
            }

            target.addGameObject(goRef);

            trackingVFX.add(goRef);

            routineInstanceRef.setSignalPayload(goRef);
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
