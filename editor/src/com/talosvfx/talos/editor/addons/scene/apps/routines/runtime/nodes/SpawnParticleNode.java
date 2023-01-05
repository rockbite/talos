package com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.nodes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.RoutineNode;
import com.talosvfx.talos.editor.addons.scene.apps.routines.runtime.TickableNode;
import com.talosvfx.talos.editor.addons.scene.assets.GameAsset;
import com.talosvfx.talos.editor.addons.scene.logic.GameObject;
import com.talosvfx.talos.editor.addons.scene.logic.components.ParticleComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.SpineRendererComponent;
import com.talosvfx.talos.editor.addons.scene.logic.components.TransformComponent;
import com.talosvfx.talos.editor.serialization.VFXProjectData;
import com.talosvfx.talos.editor.utils.NamingUtils;
import com.talosvfx.talos.runtime.ParticleEffectInstance;


public class SpawnParticleNode extends RoutineNode implements TickableNode {

    private Vector2 tmp = new Vector2();

    private Array<GameObject> trackingVFX = new Array();

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
                    trackingVFX.removeIndex(i);
                    gameObject.getParent().removeObject(gameObject);
                }
            }
        }
    }
}
