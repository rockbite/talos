package com.rockbite.tools.talos.editor.serialization;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.rockbite.tools.talos.editor.ParticleEmitterWrapper;
import com.rockbite.tools.talos.editor.data.ModuleWrapperGroup;
import com.rockbite.tools.talos.editor.widgets.ui.ModuleBoardWidget;
import com.rockbite.tools.talos.editor.wrappers.ModuleWrapper;

public class ProjectData {

	private MetaData metaData;

	private Array<EmitterData> emitters = new Array<>();

	public ProjectData () {

	}

	public void setFrom (ModuleBoardWidget moduleBoardWidget) {
		final ObjectMap<ParticleEmitterWrapper, Array<ModuleWrapper>> moduleWrappers = moduleBoardWidget.moduleWrappers;
		final ObjectMap<ParticleEmitterWrapper, Array<ModuleBoardWidget.NodeConnection>> nodeConnections = moduleBoardWidget.nodeConnections;

		emitters.clear();

		for (ParticleEmitterWrapper key : moduleWrappers.keys()) {
			final EmitterData emitterData = new EmitterData();
			emitterData.name = key.getName();
			emitterData.modules.addAll(moduleWrappers.get(key));

			final Array<ModuleBoardWidget.NodeConnection> nodeConns = nodeConnections.get(key);
			if(nodeConns != null) {
				for (ModuleBoardWidget.NodeConnection nodeConn : nodeConns) {
					emitterData.connections.add(new ConnectionData(nodeConn.fromModule.getId(), nodeConn.toModule.getId(), nodeConn.fromSlot, nodeConn.toSlot));
				}
			}

			// add groups
			for(ModuleWrapperGroup group: moduleBoardWidget.getGroups(key)) {
				GroupData groupData = new GroupData();
				groupData.text = group.getText();
				groupData.modules = new Array<>();
				groupData.color = group.getFrameColor().toFloatBits();
				for(ModuleWrapper wrapper: group.getModuleWrappers()) {
					groupData.modules.add(wrapper.getId());
				}
				emitterData.groups.add(groupData);
			}

			emitters.add(emitterData);
		}
	}

	public Array<EmitterData> getEmitters() {
		return emitters;
	}
}
