package com.rockbite.tools.talos.editor.serialization;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.rockbite.tools.talos.editor.ParticleEmitterWrapper;
import com.rockbite.tools.talos.editor.widgets.ui.ModuleBoardWidget;
import com.rockbite.tools.talos.editor.wrappers.ModuleWrapper;
import com.rockbite.tools.talos.runtime.modules.Module;

public class ExportData {

    private Array<EmitterExportData> emitters = new Array<>();

    public class EmitterExportData {
        public String name;
        public Array<Module> modules = new Array<>();
        public Array<ConnectionData> connections = new Array<>();
    }

    public void setFrom (ModuleBoardWidget moduleBoardWidget) {
        final ObjectMap<ParticleEmitterWrapper, Array<ModuleWrapper>> moduleWrappers = moduleBoardWidget.moduleWrappers;
        final ObjectMap<ParticleEmitterWrapper, Array<ModuleBoardWidget.NodeConnection>> nodeConnections = moduleBoardWidget.nodeConnections;

        emitters.clear();

        for (ParticleEmitterWrapper key : moduleWrappers.keys()) {
            final EmitterExportData emitterData = new EmitterExportData();
            emitterData.name = key.getName();
            for(ModuleWrapper wrapper : moduleWrappers.get(key)) {
                emitterData.modules.add(wrapper.getModule());
            }

            final Array<ModuleBoardWidget.NodeConnection> nodeConns = nodeConnections.get(key);
            if(nodeConns != null) {
                for (ModuleBoardWidget.NodeConnection nodeConn : nodeConns) {
                    emitterData.connections.add(new ConnectionData(nodeConn.fromModule.getId(), nodeConn.toModule.getId(), nodeConn.fromSlot, nodeConn.toSlot));
                }
            }

            emitters.add(emitterData);
        }

    }
}
