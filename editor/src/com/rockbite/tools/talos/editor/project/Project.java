package com.rockbite.tools.talos.editor.project;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.EmitterWrapper;
import com.rockbite.tools.talos.editor.serialization.ProjectData;
import com.rockbite.tools.talos.editor.serialization.ProjectSerializer;
import com.rockbite.tools.talos.runtime.ModuleGraph;
import com.rockbite.tools.talos.runtime.ParticleEffect;
import com.rockbite.tools.talos.runtime.ParticleEffectDescriptor;
import com.rockbite.tools.talos.runtime.ParticleSystem;
import com.sun.org.apache.xpath.internal.domapi.XPathStylesheetDOM3Exception;

public class Project {

	private ProjectData projectData;

	private ProjectSerializer projectSerializer;

	private Array<EmitterWrapper> activeWrappers = new Array<>();

	private ParticleEffect particleEffect;
	private ParticleEffectDescriptor particleEffectDescriptor;
	private EmitterWrapper currentEmitterWrapper;

	private ParticleSystem particleSystem;


	public Project () {
		projectSerializer = new ProjectSerializer();
		particleSystem = new ParticleSystem();
		particleEffect = new ParticleEffect();
		particleEffectDescriptor = new ParticleEffectDescriptor();
		particleEffect.init(particleEffectDescriptor);
		particleSystem.addEffect(particleEffect);
	}

	public void loadProject (FileHandle projectFileHandle) {
		if (projectFileHandle.exists()) {
			projectData = projectSerializer.read(projectFileHandle);
		} else {
			//Error handle
		}
	}

	public void saveProject (FileHandle destination) {
		projectData.setFrom(TalosMain.Instance().NodeStage().moduleBoardWidget);
		projectSerializer.write(destination, projectData);
	}

	public void newProject () {
		projectData = new ProjectData();
	}

	public ParticleSystem getParticleSystem () {
		return particleSystem;
	}

	public EmitterWrapper createNewEmitter (String emitterName) {
		EmitterWrapper emitterWrapper = new EmitterWrapper();
		emitterWrapper.setName(emitterName);

		ModuleGraph graph = particleSystem.createEmptyEmitter(particleEffectDescriptor);
		emitterWrapper.setModuleGraph(graph);

		activeWrappers.add(emitterWrapper);
		currentEmitterWrapper = emitterWrapper;

		TalosMain.Instance().NodeStage().moduleBoardWidget.setCurrentEmitter(currentEmitterWrapper);
		TalosMain.Instance().UIStage().setEmitters(activeWrappers);

		return emitterWrapper;
	}


	public void addEmitter (EmitterWrapper emitterWrapper) {
		activeWrappers.add(emitterWrapper);
	}

	public void removeEmitter (EmitterWrapper wrapper) {
		particleEffect.removeEmitter(wrapper.getEmitter());
		particleEffectDescriptor.removeEmitter(wrapper.getEmitter());

		activeWrappers.removeValue(wrapper, true);
		TalosMain.Instance().NodeStage().onEmitterRemoved(wrapper);

		currentEmitterWrapper = activeWrappers.peek();
		TalosMain.Instance().UIStage().setEmitters(activeWrappers);

	}

	public void setCurrentEmitterWrapper (EmitterWrapper emitterWrapper) {
		this.currentEmitterWrapper = emitterWrapper;
	}

	public EmitterWrapper getCurrentEmitterWrapper () {
		return currentEmitterWrapper;
	}

	public ModuleGraph getCurrentModuleGraph () {
		return currentEmitterWrapper.getGraph();
	}


}
