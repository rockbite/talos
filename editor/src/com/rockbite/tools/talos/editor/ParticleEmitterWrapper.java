package com.rockbite.tools.talos.editor;

import com.rockbite.tools.talos.runtime.ParticleEmitterDescriptor;

public class ParticleEmitterWrapper {

    private String emitterName = "";
    private boolean isMuted;
    private boolean isSolo;
    private int position;

    private ParticleEmitterDescriptor moduleGraph;

    public ParticleEmitterDescriptor getGraph() {
        return moduleGraph;
    }

    public void setModuleGraph(ParticleEmitterDescriptor graph) {
        this.moduleGraph = graph;
    }

    public String getName() {
        return emitterName;
    }

    public void setName(String emitterName) {
        this.emitterName = emitterName;
    }

    public ParticleEmitterDescriptor getEmitter() {
        return moduleGraph;
    }
}
