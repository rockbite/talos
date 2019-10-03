package com.rockbite.tools.talos.runtime;

import com.badlogic.gdx.utils.*;
import com.rockbite.tools.talos.runtime.modules.*;
import com.rockbite.tools.talos.runtime.modules.Module;

public class ParticleEmitterDescriptor {

    private final ParticleEffectDescriptor particleEffectResourceDescriptor;

    public ScopePayload scopePayload;

    Array<Module> modules = new Array<>(Module.class);

    ParticleModule particleModule;
    EmitterModule emitterModule;

    public static ObjectSet<Class> registeredModules;

    public ParticleEmitterDescriptor (ParticleEffectDescriptor descriptor) {
        this.particleEffectResourceDescriptor = descriptor;
        registerModules();
    }

    public static ObjectSet<Class> getRegisteredModules() {
        registerModules();
        return registeredModules;
    }

    public static void registerModules() {
        if(registeredModules == null) {
            registeredModules = new ObjectSet<>();
            registeredModules.add(EmitterModule.class);
            registeredModules.add(InterpolationModule.class);
            registeredModules.add(InputModule.class);
            registeredModules.add(ParticleModule.class);
            registeredModules.add(StaticValueModule.class);
            registeredModules.add(RandomRangeModule.class);
            registeredModules.add(MixModule.class);
            registeredModules.add(MathModule.class);
            registeredModules.add(CurveModule.class);
            registeredModules.add(Vector2Module.class);
            registeredModules.add(ColorModule.class);
            registeredModules.add(DynamicRangeModule.class);
            registeredModules.add(ScriptModule.class);
            registeredModules.add(GradientColorModule.class);
            registeredModules.add(TextureModule.class);
            registeredModules.add(EmConfigModule.class);
            registeredModules.add(OffsetModule.class);
        }
    }

    public boolean addModule (Module module) {
        boolean added = true;
        if (module instanceof ParticleModule) {
            if (particleModule == null) {
                particleModule = (ParticleModule)module;
            } else {
                added = false;
            }
        }
        if (module instanceof EmitterModule) {
            if (emitterModule == null) {
                emitterModule = (EmitterModule)module;
            } else {
                added = false;
            }
        }

        if (added) {
            modules.add(module);
        }

        return added;

    }

    public void removeModule(Module module) {
        // was this module connected to someone?
        for(Module toModule: modules) {
            if(toModule.isConnectedTo(module)) {
                toModule.detach(module);
            }
        }

        modules.removeValue(module, true);

        if(module instanceof ParticleModule) {
            particleModule = null;
        }
        if(module instanceof EmitterModule) {
            emitterModule = null;
        }
    }

    public void connectNode(Module from, Module to, int slotFrom, int slotTo) {
        // slotTo is the input of module to
        // slotFrom is the output of slot from
        from.attachModuleToMyOutput(to, slotFrom, slotTo);
        to.attachModuleToMyInput(from, slotTo, slotFrom);
    }

    public void removeNode(Module module, int slot,boolean isInput) {
        module.detach(slot, isInput);
    }

    public ParticleModule getParticleModule() {
        return particleModule;
    }

    public void resetRequesters() {
        for (int i = 0, n = modules.size; i < n; i++)
            modules.items[i].lastRequester = -1f;
    }

    public EmitterModule getEmitterModule() {
        return emitterModule;
    }

    public Array<Module> getModules() {
        return modules;
    }

    public void setScope (ScopePayload scope) {
        this.scopePayload = scope;
    }

    public ScopePayload getScope () {
        return scopePayload;
    }

    public void read(Json json, JsonValue emitter) {
        JsonValue modulesArr = emitter.get("modules");
        JsonValue connections = emitter.get("connections");
        modules = json.readValue(Array.class, modulesArr);
        Array<Module> mod = new Array<>(Module.class);
        mod.addAll(modules);
        modules = mod;
        IntMap<Module> idMap = new IntMap<>();
        for(Module module: modules) {
            module.setModuleGraph(this);
            if (module instanceof ParticleModule) {
                particleModule = (ParticleModule) module;
            }
            if (module instanceof EmitterModule) {
                emitterModule = (EmitterModule) module;
            }
            idMap.put(module.getIndex(), module);
        }
        for(JsonValue connection: connections) {
            int moduleFromId = connection.getInt("moduleFrom", 0);
            int moduleToId = connection.getInt("moduleTo", 0);
            int slotFrom = connection.getInt("slotFrom", 0);
            int slotTo = connection.getInt("slotTo", 0);

            Module moduleFrom = idMap.get(moduleFromId);
            Module moduleTo = idMap.get(moduleToId);
            connectNode(moduleFrom, moduleTo, slotFrom, slotTo);
        }
    }

    public ParticleEffectDescriptor getEffectDescriptor() {
        return particleEffectResourceDescriptor;
    }
}
