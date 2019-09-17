package com.rockbite.tools.talos.runtime;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class ParticleEffect implements Pool.Poolable {

    private ParticleEffectDescriptor descriptor;

    private Array<ParticleEmitter> emitters = new Array();

    private ParticleSystem system;

    public ParticleEffect() {

    }

   public void init(ParticleEffectDescriptor descriptor) {
        // create all the stuff
       this.descriptor = descriptor;
   }

    public void load() {
        //TODO: load from data
        ParticleEmitter emitter = new ParticleEmitter(system);
        emitters.add(emitter);
    }

    public void update(float delta) {
        for(int i = 0; i < emitters.size; i++) {
            emitters.get(i).update(delta);
        }
    }

    @Override
    public void reset() {

    }

    public boolean isTypeOf(ParticleEffectDescriptor particleEffectDescriptor) {
        return descriptor == particleEffectDescriptor;
    }

    public void createEmitter(ModuleGraph moduleGraph) {
        ParticleEmitter emitter = new ParticleEmitter(system, moduleGraph);
        emitters.add(emitter);
    }

    public Array<ParticleEmitter> getEmitters() {
        return emitters;
    }
}
