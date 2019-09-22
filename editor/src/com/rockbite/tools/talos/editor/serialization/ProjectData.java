package com.rockbite.tools.talos.editor.serialization;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.rockbite.tools.talos.editor.EmitterWrapper;
import com.rockbite.tools.talos.editor.project.Project;
import com.rockbite.tools.talos.editor.widgets.ui.ModuleBoardWidget;
import com.rockbite.tools.talos.editor.wrappers.ModuleWrapper;
import com.rockbite.tools.talos.runtime.ParticleEffect;

public class ProjectData {

	private MetaData metaData;

	private Array<EmitterData> emitters = new Array<>();

	public ProjectData () {

	}

	public void setFrom (ModuleBoardWidget moduleBoardWidget) {
		final ObjectMap<EmitterWrapper, Array<ModuleWrapper>> moduleWrappers = moduleBoardWidget.moduleWrappers;
		final ObjectMap<EmitterWrapper, Integer> idMap = moduleBoardWidget.idMap;
		final ObjectMap<EmitterWrapper, Array<ModuleBoardWidget.NodeConnection>> nodeConnections = moduleBoardWidget.nodeConnections;

		emitters.clear();

		for (ObjectMap.Entry<EmitterWrapper, Integer> emitterWrapperIntegerEntry : idMap) {
			final EmitterWrapper key = emitterWrapperIntegerEntry.key;
			final EmitterData emitterData = new EmitterData();
			emitterData.name = key.getName();
			emitterData.modules.addAll(moduleWrappers.get(key));

			final Array<ModuleBoardWidget.NodeConnection> nodeConns = nodeConnections.get(key);
			for (ModuleBoardWidget.NodeConnection nodeConn : nodeConns) {
				emitterData.connections.add(new ConnectionData(nodeConn.fromModule.getId(), nodeConn.toModule.getId(), nodeConn.fromSlot, nodeConn.toSlot));
			}

			emitters.add(emitterData);
		}
	}

	public Array<EmitterData> getEmitters() {
		return emitters;
	}
}
