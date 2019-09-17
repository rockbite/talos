package com.rockbite.tools.talos.editor;

import com.badlogic.gdx.utils.JsonValue;
import com.rockbite.tools.talos.runtime.ModuleGraph;

public class EmitterWrapper {

    private String emitterName = "";
    private boolean isMuted;
    private boolean isSolo;
    private int position;

    private ModuleGraph moduleGraph;

    public ModuleGraph getGraph() {
        return moduleGraph;
    }

    public void setModuleGraph(ModuleGraph graph) {
        this.moduleGraph = graph;
    }

    public String getName() {
        return emitterName;
    }
}
