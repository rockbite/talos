package com.rockbite.tools.talos.runtime;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;

public class ParticleEffectDescriptor {

    /**
     * graph per each emitter
     */
    private Array<ModuleGraph> graphList = new Array<>();

    public ParticleEffectDescriptor() {

    }

    public void load(FileHandle file) {
        //iterate through emitters and create their graphs
    }

    public ModuleGraph getGraph(int emitterId) {

        return null;
    }

    public ModuleGraph createEmitter(ParticleSystem system) {
        ModuleGraph graph = new ModuleGraph(system);
        graphList.add(graph);

        return graph;
    }

    public void removeEmitter(ModuleGraph emitter) {
        graphList.removeValue(emitter, true);
    }
}
