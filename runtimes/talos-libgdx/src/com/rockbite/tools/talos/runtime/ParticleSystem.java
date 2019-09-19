package com.rockbite.tools.talos.runtime;


import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.rockbite.tools.talos.runtime.modules.EmitterModule;
import com.rockbite.tools.talos.runtime.modules.ParticleModule;

public class ParticleSystem {

    private Array<ParticleEffectDescriptor> particleEffects = new Array<>();
    private Array<ParticleEffect> particleEffectInstances = new Array<>();

    private ScopePayload scopePayload = new ScopePayload(); // temporary location

    public ParticleSystem() {

    }

    public void update(float delta) {
        for(int i = 0; i < particleEffectInstances.size; i++) {
            particleEffectInstances.get(i).update(delta);
        }
    }

    public ParticleEffectDescriptor loadEffect(FileHandle file) {
        ParticleEffectDescriptor particleEffectDescriptor = new ParticleEffectDescriptor();
        particleEffectDescriptor.load(file);
        particleEffects.add(particleEffectDescriptor);

        return particleEffectDescriptor;
    }

    public void unloadEffect(ParticleEffectDescriptor particleEffectDescriptor) {
        particleEffects.removeValue(particleEffectDescriptor, true);
    }

    public void createEffect(ParticleEffectDescriptor descriptor) {
        ParticleEffect effect = new ParticleEffect(); // make this pooled
        effect.init(descriptor);
        particleEffects.add(descriptor);
        addEffect(effect);
    }

    public void addEffect(ParticleEffect effect) {
        particleEffectInstances.add(effect);
    }

    public void removeEffect(ParticleEffect effect) {
        particleEffectInstances.removeValue(effect, true);
    }

    public EmitterModule getEmitterModuleFor(ParticleEffectDescriptor descriptor, int emitterId) {
        return descriptor.getGraph(emitterId).emitterModule;
    }

    public ParticleModule getParticleModuleFor(ParticleEffectDescriptor descriptor, int emitterId) {
        return descriptor.getGraph(emitterId).particleModule;
    }

    public ScopePayload getScope() {
        return scopePayload;
    }

    public ModuleGraph createEmptyEmitter(ParticleEffectDescriptor particleEffectDescriptor) {
        ModuleGraph moduleGraph = particleEffectDescriptor.createEmitter(this);

        // iterate all the instances and add the emitter there
        for(ParticleEffect effect: particleEffectInstances) {
            if(effect.isTypeOf(particleEffectDescriptor)) {
                effect.createEmitter(moduleGraph);
            }
        }

        return moduleGraph;
    }

    public  Array<ParticleEffect> getEffectInstances() {
        return particleEffectInstances;
    }

    public  Array<ParticleEffectDescriptor> getEffectDescriptors() {
        return particleEffects;
    }

    public void removeEmitter(ParticleEffectDescriptor effect, ModuleGraph emitter) {

        for(ParticleEffect eff: getEffectInstances()) {
            if(eff.getDescriptor() == effect) {
                eff.removeEmitter(emitter);
            }
        }

        effect.removeEmitter(emitter);
    }

    public void clearEffect(ParticleEffectDescriptor effect) {
        for(ParticleEffect eff: getEffectInstances()) {
            if(eff.getDescriptor() == effect) {
                eff.removeEmitters();
            }
        }
        effect.removeEmitters();
    }
}
