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

package com.rockbite.tools.talos.editor.project;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.ParticleEmitterWrapper;
import com.rockbite.tools.talos.editor.LegacyImporter;
import com.rockbite.tools.talos.editor.assets.TalosAssetProvider;
import com.rockbite.tools.talos.editor.data.ModuleWrapperGroup;
import com.rockbite.tools.talos.editor.dialogs.SettingsDialog;
import com.rockbite.tools.talos.editor.serialization.*;
import com.rockbite.tools.talos.editor.widgets.ui.ModuleBoardWidget;
import com.rockbite.tools.talos.editor.wrappers.ModuleWrapper;
import com.rockbite.tools.talos.runtime.ParticleEmitterDescriptor;
import com.rockbite.tools.talos.runtime.ParticleEffectInstance;
import com.rockbite.tools.talos.runtime.ParticleEffectDescriptor;
import com.rockbite.tools.talos.runtime.modules.TextureModule;
import com.rockbite.tools.talos.runtime.serialization.ConnectionData;
import com.rockbite.tools.talos.runtime.serialization.ExportData;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Comparator;

public class TalosProject implements IProject {

	private ProjectData projectData;
	private ProjectSerializer projectSerializer;
	private Array<ParticleEmitterWrapper> activeWrappers = new Array<>();
	private ParticleEffectInstance particleEffect;
	private ParticleEffectDescriptor particleEffectDescriptor;
	private ParticleEmitterWrapper currentEmitterWrapper;
	private LegacyImporter importer;
	private TalosAssetProvider projectAssetProvider;
	private MetaData readMetaData;

	public TalosProject() {
		projectAssetProvider = new TalosAssetProvider();

		projectSerializer = new ProjectSerializer();
		particleEffectDescriptor = new ParticleEffectDescriptor();
		particleEffectDescriptor.setAssetProvider(projectAssetProvider);
		particleEffect = new ParticleEffectInstance(particleEffectDescriptor);
		particleEffect.setScope(TalosMain.Instance().globalScope);
		particleEffect.loopable = true;

		importer = new LegacyImporter(TalosMain.Instance().NodeStage());
	}


	public void loadProject (String data) {
		TalosMain.Instance().UIStage().PreviewWidget().getGLProfiler().reset();

		cleanData();

		projectSerializer.prereadhack(data);
		projectData = projectSerializer.read(data);
		readMetaData = projectData.metaData;

		ParticleEmitterWrapper firstEmitter = null;

		for(EmitterData emitterData: projectData.getEmitters()) {
			IntMap<ModuleWrapper> map = new IntMap<>();

			ParticleEmitterWrapper emitterWrapper = createNewEmitter(emitterData.name, emitterData.sortPosition);

			TalosMain.Instance().NodeStage().moduleBoardWidget.loadEmitterToBoard(emitterWrapper, emitterData);

			final ParticleEmitterDescriptor graph = emitterWrapper.getGraph();
			for (ModuleWrapper module : emitterData.modules) {
				map.put(module.getId(), module);

				graph.addModule(module.getModule());
				module.getModule().setModuleGraph(graph);
			}


			// time to load groups here
			for(GroupData group: emitterData.groups) {
				ObjectSet<ModuleWrapper> childWrappers = new ObjectSet<>();
				for(Integer id: group.modules) {
					if(map.get(id) != null) {
						childWrappers.add(map.get(id));
					}
				}
				ModuleWrapperGroup moduleWrapperGroup = TalosMain.Instance().NodeStage().moduleBoardWidget.createGroupForWrappers(childWrappers);
				Color clr = new Color();
				Color.abgr8888ToColor(clr, group.color);
				moduleWrapperGroup.setData(group.text, clr);
			}
		}

		sortEmitters();

		if(activeWrappers.size > 0) {
			firstEmitter = activeWrappers.first();
		}

		if(firstEmitter != null) {
			TalosMain.Instance().TalosProject().setCurrentEmitterWrapper(firstEmitter);
			TalosMain.Instance().NodeStage().moduleBoardWidget.setCurrentEmitter(firstEmitter);
		}

		TalosMain.Instance().UIStage().setEmitters(activeWrappers);
	}

	public void sortEmitters() {
		particleEffect.sortEmitters();
		Comparator comparator = new Comparator<ParticleEmitterWrapper>() {
			@Override
			public int compare(ParticleEmitterWrapper o1, ParticleEmitterWrapper o2) {
				return o1.getEmitter().getSortPosition() - o2.getEmitter().getSortPosition();
			}
		};
		activeWrappers.sort(comparator);
		TalosMain.Instance().UIStage().setEmitters(activeWrappers);
	}

	public String getProjectString () {
		projectData.setFrom(TalosMain.Instance().NodeStage().moduleBoardWidget);
		String data = projectSerializer.write(projectData);

		return data;
	}

	public void resetToNew(){
		cleanData();
		projectData = new ProjectData();
		createNewEmitter("default_emitter", 0);
	}

	@Override
	public String getExtension() {
		return ".tls";
	}

	@Override
	public String getExportExtension() {
		return ".p";
	}

	@Override
	public String getProjectNameTemplate() {
		return "effect";
	}

	@Override
	public void initUIContent() {

	}

	@Override
	public Array<String> getSavedResourcePaths () {
		return readMetaData.getResourcePathStrings();
	}

	@Override
	public FileHandle findFileInDefaultPaths(String fileName) {
		String path = TalosMain.Instance().Prefs().getString(SettingsDialog.ASSET_PATH);
		FileHandle handle = Gdx.files.absolute(path + File.separator + fileName);
		return handle;
	}


	public void exportProject(FileHandle handle) {
		ExportData exportData = new ExportData();
		setToExportData(exportData, TalosMain.Instance().NodeStage().moduleBoardWidget);
		handle.writeString(projectSerializer.writeExport(exportData), false);
	}

	@Override
	public String exportProject() {
		ExportData exportData = new ExportData();
		setToExportData(exportData, TalosMain.Instance().NodeStage().moduleBoardWidget);
		return projectSerializer.writeExport(exportData);
	}

	private void cleanData() {
		TalosMain.Instance().UIStage().PreviewWidget().resetToDefaults();

		TalosMain.Instance().NodeStage().moduleBoardWidget.clearAll();
		activeWrappers.clear();
		particleEffectDescriptor = new ParticleEffectDescriptor();
		particleEffectDescriptor.setAssetProvider(projectAssetProvider);
		particleEffect = new ParticleEffectInstance(particleEffectDescriptor);
		particleEffect.setScope(TalosMain.Instance().globalScope);
		particleEffect.loopable = true;

		TalosMain.Instance().UIStage().setEmitters(activeWrappers);
	}

	public ParticleEffectInstance getParticleEffect () {
		return particleEffect;
	}

	public ParticleEmitterWrapper createNewEmitter (String emitterName, int sortPosition) {
		ParticleEmitterWrapper emitterWrapper = new ParticleEmitterWrapper();
		emitterWrapper.setName(emitterName);

		ParticleEmitterDescriptor moduleGraph = TalosMain.Instance().TalosProject().particleEffectDescriptor.createEmitterDescriptor();
		emitterWrapper.setModuleGraph(moduleGraph);

		activeWrappers.add(emitterWrapper);
		currentEmitterWrapper = emitterWrapper;
		if(sortPosition == -1) {
			moduleGraph.setSortPosition(particleEffect.getEmitters().size);
		} else {
			moduleGraph.setSortPosition(sortPosition);
		}

		particleEffect.addEmitter(moduleGraph);
		if(sortPosition == -1) {
			sortEmitters();
		} else {
			TalosMain.Instance().UIStage().setEmitters(activeWrappers);
		}


		TalosMain.Instance().NodeStage().moduleBoardWidget.setCurrentEmitter(currentEmitterWrapper);


		return emitterWrapper;
	}


	public void addEmitter (ParticleEmitterWrapper emitterWrapper) {
		activeWrappers.add(emitterWrapper);
	}

	public void removeEmitter (ParticleEmitterWrapper wrapper) {
		particleEffect.removeEmitterForEmitterDescriptor(wrapper.getEmitter());
		particleEffectDescriptor.removeEmitter(wrapper.getEmitter());

		activeWrappers.removeValue(wrapper, true);
		if (activeWrappers.size > 0) {
			currentEmitterWrapper = activeWrappers.peek();
		} else {
			currentEmitterWrapper = null;
		}
		TalosMain.Instance().NodeStage().onEmitterRemoved(wrapper);
		TalosMain.Instance().UIStage().setEmitters(activeWrappers);

	}

	public void setCurrentEmitterWrapper (ParticleEmitterWrapper emitterWrapper) {
		this.currentEmitterWrapper = emitterWrapper;
	}

	public ParticleEmitterWrapper getCurrentEmitterWrapper () {
		return currentEmitterWrapper;
	}

	public ParticleEmitterDescriptor getCurrentModuleGraph () {
		return currentEmitterWrapper.getGraph();
	}

	public TalosAssetProvider getProjectAssetProvider () {
		return projectAssetProvider;
	}

	public String getLocalPath() {
		try {
			return new File(this.getClass().getProtectionDomain().getCodeSource().getLocation()
					.toURI()).getParent();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		return "";
	}

	public void importFromLegacyFormat(FileHandle fileHandle) {
		cleanData();
		importer.read(fileHandle);
	}

	private void setToExportData (ExportData data, ModuleBoardWidget moduleBoardWidget) {
		final ObjectMap<ParticleEmitterWrapper, Array<ModuleWrapper>> moduleWrappers = moduleBoardWidget.moduleWrappers;
		final ObjectMap<ParticleEmitterWrapper, Array<ModuleBoardWidget.NodeConnection>> nodeConnections = moduleBoardWidget.nodeConnections;

		for (ParticleEmitterWrapper key : moduleWrappers.keys()) {
			final ExportData.EmitterExportData emitterData = new ExportData.EmitterExportData();
			emitterData.name = key.getName();
			for (ModuleWrapper wrapper : moduleWrappers.get(key)) {
				emitterData.modules.add(wrapper.getModule());

				if (wrapper.getModule() instanceof TextureModule) {
					TextureModule textureModule = (TextureModule)wrapper.getModule();
					String name = textureModule.regionName;
					if (name == null)
						name = "fire";
					if (name.contains(".")) {
						name = name.substring(0, name.lastIndexOf("."));
					}
					if (!data.metadata.resources.contains(name, false)) {
						data.metadata.resources.add(name);
					}
				}
			}

			final Array<ModuleBoardWidget.NodeConnection> nodeConns = nodeConnections.get(key);
			if (nodeConns != null) {
				for (ModuleBoardWidget.NodeConnection nodeConn : nodeConns) {
					emitterData.connections.add(new ConnectionData(nodeConn.fromModule.getModule().getIndex(), nodeConn.toModule.getModule().getIndex(), nodeConn.fromSlot, nodeConn.toSlot));
				}
			}

			data.emitters.add(emitterData);
		}

	}

	public Array<ParticleEmitterWrapper> getActiveWrappers() {
		return activeWrappers;
	}
}
