package com.rockbite.tools.talos.runtime;


import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import java.util.HashMap;

public class ParticleSystem {

    Array<ParticleEmitter> emitters = new Array();

    private ValuePool valuePool;
    private ModuleGraph moduleGraph;

    public ParticleSystem() {
        moduleGraph = new ModuleGraph(this);
        valuePool = new ValuePool();

        /*
        try {
            EmitterModule emitterModule = (EmitterModule) moduleGraph.createModule(EmitterModule.class);
            ParticleModule particleModule = (ParticleModule) moduleGraph.createModule(ParticleModule.class);

            //StaticValueModule staticValueModule = (StaticValueModule) moduleGraph.createModule(StaticValueModule.class);
            InterpolationModule formulaValueModule = (InterpolationModule) moduleGraph.createModule(InterpolationModule.class);
            InputModule inputModule = (InputModule) moduleGraph.createModule(InputModule.class);

            InterpolationModule formulaValueModule1 = (InterpolationModule) moduleGraph.createModule(InterpolationModule.class);
            formulaValueModule1.setExpression(InterpolationModule.Expression.EXP2);
            InputModule inputModule1 = (InputModule) moduleGraph.createModule(InputModule.class);

            //moduleGraph.connectNode(staticValueModule, emitterModule, 0, 0);
            moduleGraph.connectNode(formulaValueModule, emitterModule, 0, 0);
            moduleGraph.connectNode(inputModule, formulaValueModule, 0, 0);

            moduleGraph.connectNode(formulaValueModule1, particleModule, 0, 0);
            moduleGraph.connectNode(inputModule1, formulaValueModule1, 0, 0);

            //staticValueModule.staticValue.set(10f);
            inputModule.setInput(ScopePayload.EMITTER_ALPHA);
            inputModule1.setInput(ScopePayload.PARTICLE_ALPHA);

            emitters.add(new ParticleEmitter(this, emitterModule));

        } catch (ReflectionException e) {
            e.printStackTrace();
        }*/

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

    public ValuePool getValuePool() {
        return valuePool;
    }

    public ParticleModule getParticleModule() {
        return moduleGraph.getParticleModule();
    }

    public ModuleGraph getModuleGraph() {
        return moduleGraph;
    }
}
