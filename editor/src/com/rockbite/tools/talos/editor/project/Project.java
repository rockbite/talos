package com.rockbite.tools.talos.editor.project;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.rockbite.tools.talos.TalosMain;
import com.rockbite.tools.talos.editor.EmitterWrapper;
import com.rockbite.tools.talos.editor.LegacyImporter;
import com.rockbite.tools.talos.editor.serialization.ProjectData;
import com.rockbite.tools.talos.editor.serialization.ProjectSerializer;
import com.rockbite.tools.talos.runtime.ModuleGraph;
import com.rockbite.tools.talos.runtime.ParticleEffect;
import com.rockbite.tools.talos.runtime.ParticleEffectDescriptor;
import com.rockbite.tools.talos.runtime.ParticleSystem;

import java.io.File;
import java.net.URISyntaxException;

public class Project {

	private ProjectData projectData;

	private ProjectSerializer projectSerializer;

	private Array<EmitterWrapper> activeWrappers = new Array<>();

	private ParticleEffect particleEffect;
	private ParticleEffectDescriptor particleEffectDescriptor;
	private EmitterWrapper currentEmitterWrapper;

	private ParticleSystem particleSystem;

	private String currentProjectPath = null;

	private LegacyImporter importer;

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
			currentProjectPath = projectFileHandle.path();
		} else {
			//Error handle
		}
	}

	public void saveProject() {
		if(isBoundToFile()) {
			FileHandle handle = Gdx.files.absolute(currentProjectPath);
			saveProject(handle);
		}
	}

	public void saveProject (FileHandle destination) {
		projectData.setFrom(TalosMain.Instance().NodeStage().moduleBoardWidget);
		projectSerializer.write(destination, projectData);

		currentProjectPath = destination.path();
	}

	public void loadDefaultProject() {
		FileHandle fileHandle = Gdx.files.internal("samples/fire.tls");
		if (fileHandle.exists()) {
			TalosMain.Instance().Project().loadProject(fileHandle);
		} else {
			// empty stuff
			TalosMain.Instance().Project().createNewEmitter("emitter1");
		}
	}

	public void newProject () {
		TalosMain.Instance().NodeStage().moduleBoardWidget.clearAll();
		projectData = new ProjectData();
		createNewEmitter("default_emitter");
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

	public String getLocalPath() {
		try {
			return new File(this.getClass().getProtectionDomain().getCodeSource().getLocation()
					.toURI()).getParent();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		return "";
	}

	public boolean isBoundToFile() {
		return currentProjectPath != null;
	}

	public void importFromLegacyFormat(FileHandle fileHandle) {
		importer.read(fileHandle);
		currentProjectPath = null;
	}
}
