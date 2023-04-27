package com.talosvfx.talos.runtime.routine.nodes;

import com.talosvfx.talos.runtime.routine.RoutineNode;
import com.talosvfx.talos.runtime.scene.GameObject;
import com.talosvfx.talos.runtime.scene.components.ParticleComponent;
import com.talosvfx.talos.runtime.vfx.ParticleEffectInstance;

public class StopParticleEffectNode extends RoutineNode {

    @Override
    public void receiveSignal(String portName) {
        GameObject target = (GameObject) routineInstanceRef.getSignalPayload();

        if(target == null) return;

        ParticleComponent component = target.getComponent(ParticleComponent.class);
        ParticleEffectInstance effectRef = component.getEffectRef();
        if(effectRef != null) {
            effectRef.allowCompletion();
        }

        sendSignal("onComplete");
    }
}
