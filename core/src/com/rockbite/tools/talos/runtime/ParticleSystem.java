package com.rockbite.tools.talos.runtime;


import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import java.util.HashMap;

public class ParticleSystem {

    Array<ParticleEmitter> emitters = new Array();

    private ModuleGraph moduleGraph;

    public ParticleSystem() {
        moduleGraph = new ModuleGraph(this);

        emitters.add(new ParticleEmitter(this));

    }

    public void setEmitterModule(EmitterModule module) {
        for(ParticleEmitter emitter: emitters) {
            emitter.setEmitterModule(module);
        }

    }

    public void update(float delta) {
        for(int i = 0; i < emitters.size; i++) {
            emitters.get(i).update(delta);
        }
    }

    public ParticleModule getParticleModule() {
        return moduleGraph.getParticleModule();
    }

    public ModuleGraph getModuleGraph() {
        return moduleGraph;
    }
}
