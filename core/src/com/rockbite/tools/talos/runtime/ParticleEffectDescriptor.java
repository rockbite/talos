package com.rockbite.tools.talos.runtime;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;

public class ParticleEffectDescriptor {

    /**
     * graph per each emitter
     */
    private IntMap<ModuleGraph> graphList = new IntMap<>();

    public ParticleEffectDescriptor() {

    }

    public void load(FileHandle file) {
        //iterate through emitters and create their graphs
    }

    public ModuleGraph getGraph(int emitterId) {

        return null;
    }
}
