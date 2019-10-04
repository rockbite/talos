package com.rockbite.tools.talos.runtime;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class ParticleEffectDescriptor {

	/**
	 * graph per each emitter
	 */
	public Array<ParticleEmitterDescriptor> emitterModuleGraphs = new Array<>();

	private TextureAtlas atlas;

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
		JsonValue root = new JsonReader().parse(fileHandle.readString());

		ParticleEmitterDescriptor.registerModules();
		for (Class clazz: ParticleEmitterDescriptor.registeredModules) {
			json.addClassTag(clazz.getSimpleName(), clazz);
		}

		JsonValue emitters = root.get("emitters");
		for(JsonValue emitter: emitters) {
			ParticleEmitterDescriptor emitterDescriptor = new ParticleEmitterDescriptor(this);
			emitterDescriptor.read(json, emitter);
			emitterModuleGraphs.add(emitterDescriptor);
		}
	}

	public ParticleEffectInstance createEffectInstance() {
		ParticleEffectInstance particleEffectInstance = new ParticleEffectInstance(this);

		for(ParticleEmitterDescriptor emitterDescriptor: emitterModuleGraphs) {
			particleEffectInstance.addEmitter(emitterDescriptor);
		}

		return particleEffectInstance;
	}

	public TextureRegion getTextureRegion(String name) {
		//remove extension
		if(name.contains(".")) {
			name = name.substring(0, name.indexOf("."));
		}
		return atlas.findRegion(name);
	}

	public boolean isContinuous() {
		for(ParticleEmitterDescriptor emitterDescriptor: emitterModuleGraphs) {
			if(emitterDescriptor.isContinuous()) {
				return true;
			}
		}

		return false;
	}

	public void setTextureAtlas(TextureAtlas atlas) {
		this.atlas = atlas;
	}
}
