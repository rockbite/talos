package com.rockbite.tools.talos.runtime;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.rockbite.tools.talos.runtime.assets.AssetProvider;
import com.rockbite.tools.talos.runtime.modules.EmitterModule;
import com.rockbite.tools.talos.runtime.modules.Module;
import com.rockbite.tools.talos.runtime.modules.ParticleModule;
import com.rockbite.tools.talos.runtime.serialization.ConnectionData;
import com.rockbite.tools.talos.runtime.serialization.ExportData;

public class ParticleEffectDescriptor {

	/**
	 * graph per each emitter
	 */
	public Array<ParticleEmitterDescriptor> emitterModuleGraphs = new Array<>();

	private AssetProvider assetProvider;

	private ParticleEffectInstance processsingEffectReference;

	public ParticleEffectDescriptor () {

	}

	public void addEmitter (ParticleEmitterDescriptor emitter) {
	    emitterModuleGraphs.add(emitter);
    }

	public void removeEmitter (ParticleEmitterDescriptor emitter) {
		emitterModuleGraphs.removeValue(emitter, true);
	}

	public ParticleEmitterDescriptor createEmitterDescriptor () {
		return new ParticleEmitterDescriptor(this);
	}

	public void load(FileHandle fileHandle) {
		Json json = new Json();
		ParticleEmitterDescriptor.registerModules();
		for (Class clazz: ParticleEmitterDescriptor.registeredModules) {
			json.addClassTag(clazz.getSimpleName(), clazz);
		}

		final ExportData exportData = json.fromJson(ExportData.class, fileHandle.readString());

		for (ExportData.EmitterExportData emitter : exportData.emitters) {
			ParticleEmitterDescriptor emitterDescriptor = new ParticleEmitterDescriptor(this);

			IntMap<Module> idMap = new IntMap<>();

			for (Module module: emitter.modules) {
				module.setModuleGraph(emitterDescriptor);
				if (module instanceof ParticleModule) {
					emitterDescriptor.particleModule = (ParticleModule)module;
				}
				if (module instanceof EmitterModule) {
					emitterDescriptor.emitterModule = (EmitterModule)module;
				}
				idMap.put(module.getIndex(), module);
				emitterDescriptor.modules.add(module); // I cannot understand how this was working before. This is needed so that it can later reset requesters.
			}

			for (ConnectionData connection : emitter.connections) {
				final int moduleFromId = connection.moduleFrom;
				final int moduleToId = connection.moduleTo;
				final int slotFrom = connection.slotFrom;
				final int slotTo = connection.slotTo;

				Module moduleFrom = idMap.get(moduleFromId);
				Module moduleTo = idMap.get(moduleToId);

				if (moduleFrom == null) {
					throw new GdxRuntimeException("No module from found for id: " + moduleFromId);
				}
				if (moduleTo == null) {
					throw new GdxRuntimeException("No module to found for id: " + moduleToId);
				}

				emitterDescriptor.connectNode(moduleFrom, moduleTo, slotFrom, slotTo);
			}

			emitterModuleGraphs.add(emitterDescriptor);
		}
	}

	public ParticleEffectInstance createEffectInstance() {
		ParticleEffectInstance particleEffectInstance = new ParticleEffectInstance(this);

		for(ParticleEmitterDescriptor emitterDescriptor: emitterModuleGraphs) {
			particleEffectInstance.addEmitter(emitterDescriptor);
		}

		particleEffectInstance.sortEmitters();

		return particleEffectInstance;
	}

	public boolean isContinuous() {
		for(ParticleEmitterDescriptor emitterDescriptor: emitterModuleGraphs) {
			if(emitterDescriptor.isContinuous()) {
				return true;
			}
		}

		return false;
	}

	public AssetProvider getAssetProvider () {
		return assetProvider;
	}

	public void setAssetProvider (AssetProvider assetProvider) {
		this.assetProvider = assetProvider;
	}

	public void setEffectReference(ParticleEffectInstance particleEffectInstance) {
		processsingEffectReference = particleEffectInstance;
	}

	public ParticleEffectInstance getInstanceReference() {
		return processsingEffectReference;
	}
}
