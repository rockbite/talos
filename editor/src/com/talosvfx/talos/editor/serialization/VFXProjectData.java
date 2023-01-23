/*******************************************************************************
 * Copyright 2019 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.talosvfx.talos.editor.serialization;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.talosvfx.talos.editor.ParticleEmitterWrapper;
import com.talosvfx.talos.editor.data.ModuleWrapperGroup;
import com.talosvfx.talos.editor.widgets.ui.ModuleBoardWidget;
import com.talosvfx.talos.editor.wrappers.ModuleWrapper;
import com.talosvfx.talos.runtime.vfx.ParticleEffectDescriptor;
import com.talosvfx.talos.runtime.vfx.serialization.BaseVFXProjectData;
import com.talosvfx.talos.runtime.vfx.serialization.ConnectionData;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Supplier;

public class VFXProjectData extends BaseVFXProjectData {


	private Array<EmitterData> emitters = new Array<>();

	@Getter
	private transient VFXEditorState editorState = new VFXEditorState();

	@Getter
	private transient Supplier<ParticleEffectDescriptor> descriptorSupplier = new Supplier<ParticleEffectDescriptor>() {
		@Override
		public ParticleEffectDescriptor get () {
			return descriptor;
		}
	};

	@Setter
	private transient ParticleEffectDescriptor descriptor;

	public VFXProjectData () {

	}

	public void setFrom (ModuleBoardWidget moduleBoardWidget) {
		final ObjectMap<ParticleEmitterWrapper, Array<ModuleWrapper>> moduleWrappers = moduleBoardWidget.moduleWrappers;
		final ObjectMap<ParticleEmitterWrapper, Array<ModuleBoardWidget.NodeConnection>> nodeConnections = moduleBoardWidget.nodeConnections;

		emitters.clear();

		for (ParticleEmitterWrapper key : moduleWrappers.keys()) {
			final EmitterData emitterData = new EmitterData();
			emitterData.name = key.getName();
			emitterData.sortPosition = key.getEmitter().getSortPosition();
			emitterData.modules.addAll(moduleWrappers.get(key));
			emitterData.isMuted = key.isMuted;

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
